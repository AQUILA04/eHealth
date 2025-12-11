import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  getHealth() {
    return {
      status: 'ok',
      service: 'audit-service',
      timestamp: new Date().toISOString(),
    };
  }
}
