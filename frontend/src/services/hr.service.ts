import apiClient from '@/lib/axios'
import type { Credential, ShiftAssignment, StaffMember } from '@/types'

export const hrService = {
  listStaff: () => apiClient.get<StaffMember[]>('/hr/staff').then((r) => r.data),
  createStaff: (data: { employeeNumber: string; firstName: string; lastName: string; department: string; position: string; clinicalStaff: boolean; email?: string; hiredOn?: string }) => apiClient.post<StaffMember>('/hr/staff', data).then((r) => r.data),
  listCredentials: () => apiClient.get<Credential[]>('/hr/credentials').then((r) => r.data),
  createCredential: (data: { staffId: number; type: string; credentialNumber: string; issuedOn: string; expiresOn: string }) => apiClient.post<Credential>('/hr/credentials', data).then((r) => r.data),
  renewCredential: (id: number, expiresOn: string) => apiClient.post<Credential>(`/hr/credentials/${id}/renew`, { expiresOn }).then((r) => r.data),
  listShifts: () => apiClient.get<ShiftAssignment[]>('/hr/shifts').then((r) => r.data),
  createShift: (data: { staffId: number; unitName: string; shiftType: string; startsAt: string; endsAt: string; notes?: string }) => apiClient.post<ShiftAssignment>('/hr/shifts', data).then((r) => r.data),
  publishShift: (id: number) => apiClient.post<ShiftAssignment>(`/hr/shifts/${id}/publish`).then((r) => r.data),
  cancelShift: (id: number) => apiClient.post<ShiftAssignment>(`/hr/shifts/${id}/cancel`).then((r) => r.data),
}
