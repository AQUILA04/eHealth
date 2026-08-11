import apiClient from '@/lib/axios'
import type { Tenant } from '@/types'

interface WrappedResponse<T> {
  status: string
  statusCode: number
  message: string
  service: string
  data: T
}

export const tenantService = {
  list: () =>
    apiClient.get<WrappedResponse<Tenant[]>>('/tenants').then((r) => r.data.data),

  getById: (id: string) =>
    apiClient.get<WrappedResponse<Tenant>>(`/tenants/${id}`).then((r) => r.data.data),

  create: (data: Partial<Tenant>) =>
    apiClient.post<WrappedResponse<Tenant>>('/tenants', data).then((r) => r.data.data),

  update: (id: string, data: Partial<Tenant>) =>
    apiClient.put<WrappedResponse<Tenant>>(`/tenants/${id}`, data).then((r) => r.data.data),

  updateStatus: (id: string, status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED') =>
    apiClient
      .put<WrappedResponse<Tenant>>(`/tenants/${id}/status`, null, { params: { status } })
      .then((r) => r.data.data),
}
