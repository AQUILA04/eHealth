import apiClient from '@/lib/axios'
import type { LaboratoryOrder, LaboratoryPriority, LaboratoryStatus, LaboratoryInterpretation } from '@/types'

export const lisService = {
  list: (params?: { status?: LaboratoryStatus; patientRef?: string }) =>
    apiClient.get<LaboratoryOrder[]>('/lis/orders', { params }).then((response) => response.data),
  create: (data: { clinicalEncounterId: number; patientRef: string; examName: string; examCode?: string; sampleType: string; priority?: LaboratoryPriority; orderedBy?: string }) =>
    apiClient.post<LaboratoryOrder>('/lis/orders', data).then((response) => response.data),
  collect: (id: number, collectedBy: string) =>
    apiClient.patch<LaboratoryOrder>(`/lis/orders/${id}/collect`, { collectedBy }).then((response) => response.data),
  receive: (id: number, receivedBy: string) =>
    apiClient.patch<LaboratoryOrder>(`/lis/orders/${id}/receive`, { receivedBy }).then((response) => response.data),
  addResult: (id: number, data: { analyteName: string; analyteCode?: string; resultValue: string; unit?: string; referenceRange?: string; interpretation: LaboratoryInterpretation; technicalValidator?: string }) =>
    apiClient.post<LaboratoryOrder>(`/lis/orders/${id}/results`, data).then((response) => response.data),
  validate: (id: number, validatedBy: string) =>
    apiClient.patch<LaboratoryOrder>(`/lis/orders/${id}/validate`, { validatedBy }).then((response) => response.data),
  notifyCritical: (id: number, notifiedTo: string) =>
    apiClient.post<LaboratoryOrder>(`/lis/orders/${id}/critical-notification`, { notifiedTo }).then((response) => response.data),
}
