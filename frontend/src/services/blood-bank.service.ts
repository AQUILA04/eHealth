import apiClient from '@/lib/axios'
import type { BloodAboGroup, BloodComponent, BloodRhesus, BloodUnit, BloodUnitStatus, Transfusion, TransfusionStatus } from '@/types'

export const bloodBankService = {
  listUnits: (status?: BloodUnitStatus) => apiClient.get<BloodUnit[]>('/lis/blood-bank/units', { params: { status } }).then((response) => response.data),
  receiveUnit: (data: { donationCode: string; aboGroup: BloodAboGroup; rhesus: BloodRhesus; component: BloodComponent; collectedOn: string; expiresOn: string; storageLocation?: string }) => apiClient.post<BloodUnit>('/lis/blood-bank/units', data).then((response) => response.data),
  listTransfusions: (params?: { status?: TransfusionStatus; patientRef?: string }) => apiClient.get<Transfusion[]>('/lis/blood-bank/transfusions', { params }).then((response) => response.data),
  request: (data: { clinicalEncounterId: number; patientRef: string; recipientAboGroup: BloodAboGroup; recipientRhesus: BloodRhesus; component: BloodComponent; requestedBy: string }) => apiClient.post<Transfusion>('/lis/blood-bank/transfusions', data).then((response) => response.data),
  crossmatch: (id: number, validatedBy: string) => apiClient.patch<Transfusion>(`/lis/blood-bank/transfusions/${id}/crossmatch`, { validatedBy }).then((response) => response.data),
  issue: (id: number, issuedBy: string) => apiClient.patch<Transfusion>(`/lis/blood-bank/transfusions/${id}/issue`, { issuedBy }).then((response) => response.data),
  complete: (id: number, completedBy: string) => apiClient.patch<Transfusion>(`/lis/blood-bank/transfusions/${id}/complete`, { completedBy }).then((response) => response.data),
  reportReaction: (id: number, reactionDescription: string) => apiClient.post<Transfusion>(`/lis/blood-bank/transfusions/${id}/reaction`, { reactionDescription }).then((response) => response.data),
}
