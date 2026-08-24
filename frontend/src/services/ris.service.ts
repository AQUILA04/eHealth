import apiClient from '@/lib/axios'
import type { RadiologyModality, RadiologyPriority, RadiologyStatus, RadiologyStudy } from '@/types'

export const risService = {
  list: (params?: { status?: RadiologyStatus; patientRef?: string }) =>
    apiClient.get<RadiologyStudy[]>('/ris/studies', { params }).then((response) => response.data),
  create: (data: { clinicalEncounterId: number; patientRef: string; procedureName: string; procedureCode?: string; modality: RadiologyModality; priority?: RadiologyPriority; requestedBy?: string }) =>
    apiClient.post<RadiologyStudy>('/ris/studies', data).then((response) => response.data),
  schedule: (id: number, data: { scheduledAt: string; assignedTechnologist: string; assignedRadiologist?: string }) =>
    apiClient.patch<RadiologyStudy>(`/ris/studies/${id}/schedule`, data).then((response) => response.data),
  checkIn: (id: number) => apiClient.patch<RadiologyStudy>(`/ris/studies/${id}/check-in`).then((response) => response.data),
  perform: (id: number, data: { pacsStudyUid?: string; radiationDoseMgy?: number }) =>
    apiClient.patch<RadiologyStudy>(`/ris/studies/${id}/perform`, data).then((response) => response.data),
  report: (id: number, data: { reportText: string; assignedRadiologist: string }) =>
    apiClient.patch<RadiologyStudy>(`/ris/studies/${id}/report`, data).then((response) => response.data),
}
