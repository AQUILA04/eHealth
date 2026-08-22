import apiClient from '@/lib/axios'
import type { AnalyticsDashboard, MetricSnapshot } from '@/types'
export const analyticsService = {
  dashboard: () => apiClient.get<AnalyticsDashboard>('/analytics/dashboard').then(r => r.data),
  metrics: () => apiClient.get<MetricSnapshot[]>('/analytics/metrics').then(r => r.data),
  record: (data: { category: string; metricKey: string; label: string; value: number; unit?: string; sourceService?: string }) => apiClient.post<MetricSnapshot>('/analytics/metrics', data).then(r => r.data),
}
