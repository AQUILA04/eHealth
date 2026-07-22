import apiClient from '@/lib/axios'
import type { ClinicalEncounter, VitalSign, MedicationOrder, LabOrder } from '@/types'

// ─── ClinicalEncounters ───────────────────────────────────────────────────────

export const dpiEncounterService = {
  getById: (id: number) =>
    apiClient.get<ClinicalEncounter>(`/dpi/encounters/${id}`).then((r) => r.data),

  getByGapEncounter: (gapEncounterId: number) =>
    apiClient.get<ClinicalEncounter>(`/dpi/encounters/gap/${gapEncounterId}`).then((r) => r.data),

  getByPatient: (patientRef: string) =>
    apiClient.get<ClinicalEncounter[]>(`/dpi/encounters/patient/${patientRef}`).then((r) => r.data),

  create: (data: {
    gapEncounterId: number
    patientRef: string
    encounterType: string
    empiGlobalUuid?: string
    chiefComplaint?: string
    attendingPhysicianName?: string
    attendingPhysicianId?: string
    specialty?: string
  }) =>
    apiClient.post<ClinicalEncounter>('/dpi/encounters', data).then((r) => r.data),

  updateDiagnosis: (id: number, data: {
    primaryDiagnosisCode?: string
    primaryDiagnosisLabel?: string
    treatmentPlan?: string
  }) =>
    apiClient.patch<ClinicalEncounter>(`/dpi/encounters/${id}/diagnosis`, data).then((r) => r.data),

  addNote: (id: number, data: { note: string }) =>
    apiClient.patch<ClinicalEncounter>(`/dpi/encounters/${id}/notes`, data).then((r) => r.data),

  close: (id: number, data: { closingNote?: string }) =>
    apiClient.patch<ClinicalEncounter>(`/dpi/encounters/${id}/close`, data).then((r) => r.data),
}

// ─── VitalSigns ───────────────────────────────────────────────────────────────

export const dpiVitalSignService = {
  list: (encounterId: number) =>
    apiClient.get<VitalSign[]>(`/dpi/encounters/${encounterId}/vital-signs`).then((r) => r.data),

  record: (encounterId: number, data: Partial<VitalSign>) =>
    apiClient
      .post<VitalSign>(`/dpi/encounters/${encounterId}/vital-signs`, data)
      .then((r) => r.data),
}

// ─── MedicationOrders (CPOE) ──────────────────────────────────────────────────

export const dpiMedicationService = {
  list: (encounterId: number) =>
    apiClient
      .get<MedicationOrder[]>(`/dpi/encounters/${encounterId}/medications`)
      .then((r) => r.data),

  prescribe: (
    encounterId: number,
    data: {
      clinicalEncounterId?: number
      medicationName: string
      dose: string
      unit: string
      route: string
      frequency: string
      genericName?: string
      instructions?: string
      indication?: string
      prescribedBy?: string
      prescribedById?: string
    }
  ) =>
    apiClient
      .post<MedicationOrder>(`/dpi/encounters/${encounterId}/medications`, data)
      .then((r) => r.data),

  updateStatus: (encounterId: number, orderId: number, status: string) =>
    apiClient
      .patch<MedicationOrder>(`/dpi/encounters/${encounterId}/medications/${orderId}/status`, { status })
      .then((r) => r.data),
}

// ─── LabOrders ────────────────────────────────────────────────────────────────

export const dpiLabOrderService = {
  list: (encounterId: number) =>
    apiClient
      .get<LabOrder[]>(`/dpi/encounters/${encounterId}/lab-orders`)
      .then((r) => r.data),

  order: (
    encounterId: number,
    data: {
      clinicalEncounterId?: number
      orderType: string
      examName: string
      examCode?: string
      indication?: string
      priority?: string
      orderedBy?: string
      orderedById?: string
    }
  ) =>
    apiClient
      .post<LabOrder>(`/dpi/encounters/${encounterId}/lab-orders`, data)
      .then((r) => r.data),

  recordResult: (
    encounterId: number,
    orderId: number,
    data: { result: string; resultUnit?: string; referenceRange?: string; interpretation?: string }
  ) =>
    apiClient
      .patch<LabOrder>(`/dpi/encounters/${encounterId}/lab-orders/${orderId}/result`, data)
      .then((r) => r.data),
}
