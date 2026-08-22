import { Controller, All, Req, Res, HttpStatus, Logger } from '@nestjs/common';

@Controller()
export class AppController {
  private readonly logger = new Logger(AppController.name);

  // Cache for tenant statuses: tenantId -> { status: string, expiry: number }
  private tenantCache = new Map<string, { status: string; expiry: number }>();
  private CACHE_TTL_MS = 60 * 1000; // 1 minute cache

  // Service URLs (internal Docker DNS or localhost)
  private readonly GAP_URL = process.env.GAP_SERVICE_URL || 'http://localhost:8082';
  private readonly DPI_URL = process.env.DPI_SERVICE_URL || 'http://localhost:8083';
  private readonly EMPI_URL = process.env.EMPI_SERVICE_URL || 'http://localhost:8081';
    private readonly TENANT_URL = process.env.TENANT_SERVICE_URL || 'http://localhost:8084';
    private readonly LIS_URL = process.env.LIS_SERVICE_URL || 'http://localhost:8085';
    private readonly RIS_URL = process.env.RIS_SERVICE_URL || 'http://localhost:8086';
    private readonly PHARMACY_URL = process.env.PHARMACY_SERVICE_URL || 'http://localhost:8087';
    private readonly RCM_URL = process.env.RCM_SERVICE_URL || 'http://localhost:8088';

  @All('health')
  getHealth() {
    this.logger.log('Checking API Gateway health status');
    return {
      status: 'ok',
      service: 'api-gateway',
      timestamp: new Date().toISOString(),
    };
  }

  @All('api/v1/*')
  async handleProxy(@Req() req: any, @Res() res: any) {
    const path = req.path;
    this.logger.log(`Routing incoming request for path: ${path}`);

    // Determine target service base URL
    let targetBaseUrl = '';
    if (path.startsWith('/api/v1/gap/')) {
      targetBaseUrl = this.GAP_URL;
    } else if (path.startsWith('/api/v1/dpi/')) {
      targetBaseUrl = this.DPI_URL;
    } else if (path.startsWith('/api/v1/empi/')) {
      targetBaseUrl = this.EMPI_URL;
    } else if (path.startsWith('/api/v1/lis/')) {
      targetBaseUrl = this.LIS_URL;
    } else if (path.startsWith('/api/v1/ris/')) {
      targetBaseUrl = this.RIS_URL;
    } else if (path.startsWith('/api/v1/pharmacy/')) {
      targetBaseUrl = this.PHARMACY_URL;
    } else if (path.startsWith('/api/v1/rcm/')) {
      targetBaseUrl = this.RCM_URL;
    } else if (
      path.startsWith('/api/v1/tenants') ||
      path.startsWith('/api/v1/signup') ||
      path.startsWith('/api/v1/subscriptions') ||
      path.startsWith('/api/v1/internal/quota')
    ) {
      targetBaseUrl = this.TENANT_URL;
    } else {
      this.logger.warn(`Unknown route: ${path}`);
      return res.status(HttpStatus.NOT_FOUND).json({
        statusCode: 404,
        message: 'Route not found or unsupported by Gateway',
      });
    }

    // Extract authorization token and decode tenant context
    let tenantId: string | null = null;
    const authHeader = req.headers['authorization'];
    if (authHeader && authHeader.startsWith('Bearer ')) {
      const token = authHeader.substring(7);
      const payload = this.decodeJwt(token);
      if (payload) {
        tenantId = payload.tenant_id || payload.tenant;
      }
    }

    // All care-delivery endpoints require an active tenant resolved from the signed JWT.
    if (
      (path.startsWith('/api/v1/gap/') || path.startsWith('/api/v1/dpi/') ||
        path.startsWith('/api/v1/lis/') || path.startsWith('/api/v1/ris/') ||
        path.startsWith('/api/v1/pharmacy/') || path.startsWith('/api/v1/rcm/')) &&
      !path.includes('/actuator/')
    ) {
      if (!tenantId) {
        this.logger.warn('Access denied: Tenant ID missing in access token');
        return res.status(HttpStatus.UNAUTHORIZED).json({
          statusCode: 401,
          message: 'Missing tenant_id in token claims',
        });
      }

      // Check tenant status
      try {
        const isTenantActive = await this.verifyTenantActive(tenantId);
        if (!isTenantActive) {
          this.logger.warn(`Access denied: Tenant '${tenantId}' is suspended or inactive`);
          return res.status(HttpStatus.FORBIDDEN).json({
            statusCode: 403,
            message: 'Tenant is not active or suspended',
          });
        }
      } catch (err: any) {
        this.logger.error(`Error checking tenant status for '${tenantId}': ${err.message}`);
        return res.status(HttpStatus.INTERNAL_SERVER_ERROR).json({
          statusCode: 500,
          message: 'Unable to verify tenant status',
        });
      }
    }

    // Prepare proxy request headers
    const headers = { ...req.headers } as Record<string, string>;
    if (tenantId) {
      headers['x-tenant-id'] = tenantId;
    }

    // Forward the request to the backend microservice
    const targetUrl = `${targetBaseUrl}${req.originalUrl}`;
    this.logger.log(`Forwarding request to: ${targetUrl}`);

    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 30000); // 30s timeout

      const body = ['GET', 'HEAD'].includes(req.method) ? undefined : JSON.stringify(req.body);

      const response = await fetch(targetUrl, {
        method: req.method,
        headers: headers,
        body: body,
        signal: controller.signal,
      });

      clearTimeout(timeoutId);

      // Copy response headers and send status/body
      response.headers.forEach((val, key) => {
        res.setHeader(key, val);
      });

      res.status(response.status);

      const responseText = await response.text();
      try {
        const responseJson = JSON.parse(responseText);
        return res.json(responseJson);
      } catch {
        return res.send(responseText);
      }
    } catch (error: any) {
      this.logger.error(`Proxy error forwarding to ${targetUrl}: ${error.message}`);
      return res.status(HttpStatus.BAD_GATEWAY).json({
        statusCode: 502,
        message: 'Bad gateway: error connecting to upstream service',
      });
    }
  }

  private decodeJwt(token: string): any {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = Buffer.from(parts[1], 'base64').toString('utf-8');
      return JSON.parse(payload);
    } catch (e) {
      return null;
    }
  }

  private async verifyTenantActive(tenantId: string): Promise<boolean> {
    const cached = this.tenantCache.get(tenantId);
    const now = Date.now();

    if (cached && cached.expiry > now) {
      return cached.status === 'ACTIVE';
    }

    // Query tenant-service
    const url = `${this.TENANT_URL}/api/v1/tenants/${tenantId}`;
    try {
      const response = await fetch(url);
      if (response.status === 404) {
        this.tenantCache.set(tenantId, { status: 'NOT_FOUND', expiry: now + this.CACHE_TTL_MS });
        return false;
      }
      if (response.ok) {
        const responseJson = (await response.json()) as any;
        // Since responses are wrapped in Response.builder() format, extract from data.status
        const tenantStatus = responseJson.data?.status || 'INACTIVE';
        this.tenantCache.set(tenantId, { status: tenantStatus, expiry: now + this.CACHE_TTL_MS });
        return tenantStatus === 'ACTIVE';
      }
    } catch (e: any) {
      this.logger.error(`Failed to contact tenant-service for status verification: ${e.message}`);
      // Fallback: If cache exists, use stale cache. Otherwise, reject.
      if (cached) {
        return cached.status === 'ACTIVE';
      }
      throw e;
    }
    return false;
  }
}
