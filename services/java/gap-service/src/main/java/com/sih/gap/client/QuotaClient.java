package com.sih.gap.client;

import com.sih.shared.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class QuotaClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tenant.service.url:http://localhost:8084}")
    private String tenantServiceUrl;

    @Value("${tenant.internal-api-key:ehealth-internal-dev-key}")
    private String internalApiKey;

    @Value("${tenant.quota.enabled:true}")
    private boolean quotaEnabled;

    public void assertCapacity(String operation, long projectedCount) {
        assertQuota(operation, projectedCount, false);
    }

    public void assertAndRecordUsage(String operation) {
        assertQuota(operation, null, true);
    }

    private void assertQuota(String operation, Long projectedCount, boolean recordUsage) {
        if (!quotaEnabled) {
            return;
        }
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null || tenantId.isBlank()) {
            log.debug("Quota skip: no tenant in context");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("tenantId", tenantId);
        body.put("operation", operation);
        body.put("recordUsage", recordUsage);
        if (projectedCount != null) {
            body.put("projectedCount", projectedCount);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Key", internalApiKey);

        try {
            restTemplate.exchange(
                    tenantServiceUrl + "/api/v1/internal/quota/assert",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );
        } catch (HttpClientErrorException.Forbidden e) {
            throw new QuotaExceededException(operation, e.getResponseBodyAsString());
        } catch (HttpClientErrorException.BadRequest e) {
            // No subscription yet for legacy tenants — allow
            log.warn("Quota soft-skip for tenant {}: {}", tenantId, e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.warn("Quota service unreachable ({}), allowing write for tenant {}", e.getMessage(), tenantId);
        }
    }

    public static class QuotaExceededException extends RuntimeException {
        public QuotaExceededException(String operation, String details) {
            super("Quota exceeded for " + operation + ": " + details);
        }
    }
}
