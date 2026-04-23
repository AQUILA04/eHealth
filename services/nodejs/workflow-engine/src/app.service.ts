import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  getHealth() {
    return {
      status: 'ok',
      service: 'workflow-engine',
      timestamp: new Date().toISOString(),
    };
  }
}
