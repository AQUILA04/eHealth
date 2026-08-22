import apiClient from '@/lib/axios'
import type { Patient, Encounter, Appointment, BedBoardEntry } from '@/types'

// ─── Patients ─────────────────────────────────────────────────────────────────

export const gapPatientService = {
  list: () =>
    apiClient.get<Patient[]>('/gap/patients').then((r) => r.data),

  getById: (id: number) =>
    apiClient.get<Patient>(`/gap/patients/${id}`).then((r) => r.data),

  getByMrn: (mrn: string) =>
    apiClient.get<Patient>(`/gap/patients/mrn/${mrn}`).then((r) => r.data),

  search: (q: string) =>
    apiClient.get<Patient[]>('/gap/patients/search', { params: { q } }).then((r) => r.data),

  create: (data: Partial<Patient>) =>
    apiClient.post<Patient>('/gap/patients', data).then((r) => r.data),

  update: (id: number, data: Partial<Patient>) =>
    apiClient.put<Patient>(`/gap/patients/${id}`, data).then((r) => r.data),
}

// ─── Encounters (ADT) ─────────────────────────────────────────────────────────

// Mapper EncounterResponse → BedBoardEntry pour la vue bed-board
function mapToBedBoard(enc: Encounter): BedBoardEntry {
  return {
    encounterId: enc.id,
    patientName: enc.patientFullName,
    mrn: enc.patientMrn,
    ward: enc.ward,
    room: enc.room || '',
    bed: enc.bedNumber || '',
    bedStatus: enc.bedStatus || 'OCCUPIED',
    admissionDate: enc.admissionDate,
    admittingDiagnosis: enc.admissionReason,
  }
}

export const gapEncounterService = {
  getById: (id: number) =>
    apiClient.get<Encounter>(`/gap/encounters/${id}`).then((r) => r.data),

  getByPatient: (patientId: number) =>
    apiClient.get<Encounter[]>(`/gap/encounters/patient/${patientId}`).then((r) => r.data),

  getBedBoard: () =>
    apiClient.get<Encounter[]>('/gap/encounters/bed-board').then((r) =>
      r.data.map(mapToBedBoard)
    ),

  getByWard: (ward: string) =>
    apiClient.get<Encounter[]>(`/gap/encounters/ward/${ward}`).then((r) => r.data),

  admit: (data: Partial<Encounter> & { patientId?: number }) =>
    apiClient.post<Encounter>('/gap/encounters', data).then((r) => r.data),

  transfer: (id: number, data: { ward: string; room?: string; bedNumber?: string; reason?: string }) =>
    apiClient.patch<Encounter>(`/gap/encounters/${id}/transfer`, data).then((r) => r.data),

  discharge: (id: number, data: { dischargeDisposition: string; dischargeSummary?: string }) =>
    apiClient.patch<Encounter>(`/gap/encounters/${id}/discharge`, data).then((r) => r.data),
}

// ─── Appointments ─────────────────────────────────────────────────────────────

export const gapAppointmentService = {
  getByPeriod: (start: string, end: string) =>
    apiClient
      .get<Appointment[]>('/gap/appointments', { params: { start, end } })
      .then((r) => r.data),

  getByPatient: (patientId: number) =>
    apiClient.get<Appointment[]>(`/gap/appointments/patient/${patientId}`).then((r) => r.data),

  getById: (id: number) =>
    apiClient.get<Appointment>(`/gap/appointments/${id}`).then((r) => r.data),

  create: (data: Partial<Appointment>) =>
    apiClient.post<Appointment>('/gap/appointments', data).then((r) => r.data),

  updateStatus: (id: number, status: string) =>
    apiClient.patch<Appointment>(`/gap/appointments/${id}/status`, { status }).then((r) => r.data),

  cancel: (id: number, reason?: string) =>
    apiClient.delete<void>(`/gap/appointments/${id}`, { params: { reason } }).then((r) => r.data),
}
