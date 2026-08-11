// ─── Types communs ────────────────────────────────────────────────────────────

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// ─── EMPI ─────────────────────────────────────────────────────────────────────

export interface PatientIdentity {
  id: number
  globalUuid: string
  firstName: string
  lastName: string
  dateOfBirth: string
  gender: 'MALE' | 'FEMALE' | 'OTHER' | 'UNKNOWN'
  nationalId?: string
  phone?: string
  email?: string
  address?: string
  createdAt: string
}

// ─── GAP — Patients ───────────────────────────────────────────────────────────
// Aligné sur PatientResponse.java

export interface Patient {
  id: number
  localMrn: string          // backend: localMrn (affiché comme MRN)
  empiGlobalUuid?: string
  firstName: string
  lastName: string
  fullName?: string
  dateOfBirth: string
  gender: 'MALE' | 'FEMALE' | 'OTHER' | 'UNKNOWN'
  bloodGroup?: string
  phoneNumber?: string      // backend: phoneNumber
  email?: string
  address?: string
  city?: string
  nationality?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  financialCoverage?: string
  active: boolean
  createdAt: string
  updatedAt?: string
}

// ─── GAP — Encounters (ADT) ───────────────────────────────────────────────────
// Aligné sur EncounterResponse.java

export type EncounterStatus = 'PLANNED' | 'ARRIVED' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELLED'
export type EncounterType = 'INPATIENT' | 'OUTPATIENT' | 'EMERGENCY' | 'DAY_SURGERY'
export type BedStatus = 'OCCUPIED' | 'AVAILABLE' | 'CLEANING' | 'MAINTENANCE'

export interface Encounter {
  id: number
  patientId: number
  patientFullName: string   // backend: patientFullName
  patientMrn: string        // backend: patientMrn
  encounterType: EncounterType  // backend: encounterType
  status: EncounterStatus
  admissionType?: string
  admissionDate: string
  admissionReason?: string  // backend: admissionReason (= admittingDiagnosis)
  ward: string
  room?: string
  bedNumber?: string        // backend: bedNumber
  bedStatus?: BedStatus
  attendingPhysicianName?: string
  attendingPhysicianId?: string
  dischargeDate?: string
  dischargeDisposition?: string
  dischargeSummary?: string
  createdAt?: string
  updatedAt?: string
}

export interface BedBoardEntry {
  encounterId: number
  patientName: string       // backend: patientFullName (mapped in service)
  mrn: string               // backend: patientMrn (mapped in service)
  ward: string
  room: string
  bed: string               // backend: bedNumber (mapped in service)
  bedStatus: BedStatus
  admissionDate: string
  admittingDiagnosis?: string  // backend: admissionReason (mapped in service)
}

// ─── GAP — Appointments ───────────────────────────────────────────────────────
// Aligné sur AppointmentResponse.java

export type AppointmentStatus = 'SCHEDULED' | 'CONFIRMED' | 'CHECKED_IN' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW'

export interface Appointment {
  id: number
  patientId: number
  patientFullName: string   // backend: patientFullName
  patientMrn: string        // backend: patientMrn
  scheduledTime: string
  durationMinutes: number
  specialty: string
  practitionerName?: string
  practitionerId?: string
  room?: string
  reason?: string
  status: AppointmentStatus
  notes?: string
  cancellationReason?: string
  createdAt?: string
  updatedAt?: string
}

// ─── DPI — ClinicalEncounter ──────────────────────────────────────────────────
// Aligné sur ClinicalEncounterResponse.java

export type ClinicalStatus = 'OPEN' | 'CLOSED' | 'SUSPENDED'
export type ClinicalEncounterType = 'INPATIENT' | 'OUTPATIENT' | 'EMERGENCY' | 'DAY_SURGERY'

export interface ClinicalEncounter {
  id: number
  gapEncounterId: number
  patientRef: string        // backend: patientRef
  empiGlobalUuid?: string
  encounterType?: ClinicalEncounterType
  status: ClinicalStatus
  chiefComplaint?: string
  historyOfPresentIllness?: string
  pastMedicalHistory?: string
  allergies?: string
  currentMedications?: string
  physicalExamination?: string
  primaryDiagnosisCode?: string
  primaryDiagnosisLabel?: string
  secondaryDiagnosesCodes?: string
  treatmentPlan?: string
  clinicalSummary?: string
  attendingPhysicianName?: string
  attendingPhysicianId?: string
  specialty?: string
  vitalSigns?: VitalSign[]
  medicationOrders?: MedicationOrder[]
  labOrders?: LabOrder[]
  createdAt: string
  updatedAt?: string
}

// ─── DPI — VitalSigns ────────────────────────────────────────────────────────

export interface VitalSign {
  id: number
  clinicalEncounterId: number
  recordedAt: string
  recordedBy?: string
  systolicBp?: number
  diastolicBp?: number
  heartRate?: number
  temperature?: number
  respiratoryRate?: number
  oxygenSaturation?: number
  weightKg?: number
  heightCm?: number
  bmi?: number
  notes?: string
}

// ─── DPI — MedicationOrders (CPOE) ───────────────────────────────────────────
// Aligné sur MedicationOrderRequest.java (dose + unit au lieu de dosage)

export type OrderStatus = 'PENDING' | 'VALIDATED' | 'DISPENSED' | 'ADMINISTERED' | 'CANCELLED'
export type MedicationRoute = 'ORAL' | 'IV' | 'IM' | 'SC' | 'TOPICAL' | 'INHALATION' | 'OTHER'
export type MedicationFrequency = 'ONCE' | 'EVERY_4_HOURS' | 'EVERY_6_HOURS' | 'EVERY_8_HOURS' | 'EVERY_12_HOURS' | 'DAILY' | 'TWICE_DAILY' | 'THREE_TIMES_DAILY' | 'AS_NEEDED'

export interface MedicationOrder {
  id: number
  clinicalEncounterId: number
  medicationName: string
  genericName?: string
  atcCode?: string
  dose: string              // backend: dose (séparé de unit)
  unit: string              // backend: unit
  frequency: string         // backend: string (pas d'enum strict)
  route: MedicationRoute
  prescribedBy?: string     // backend: prescribedBy
  prescribedById?: string   // backend: prescribedById
  prescriberName?: string   // alias display
  prescriberId?: string     // alias display
  status: OrderStatus
  startDate?: string
  endDate?: string
  durationDays?: number
  instructions?: string
  indication?: string
  prescribedAt: string
}

// ─── DPI — LabOrders ─────────────────────────────────────────────────────────
// Aligné sur LabOrderRequest.java (orderType + priority au lieu de urgency)

export type LabOrderStatus = 'ORDERED' | 'COLLECTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type ResultInterpretation = 'NORMAL' | 'ABNORMAL_LOW' | 'ABNORMAL_HIGH' | 'CRITICAL_LOW' | 'CRITICAL_HIGH' | 'INDETERMINATE'

export interface LabOrder {
  id: number
  clinicalEncounterId: number
  orderType?: string        // backend: orderType (BIOLOGY, MICROBIOLOGY, etc.)
  examName: string
  examCode?: string
  urgency: 'ROUTINE' | 'URGENT' | 'STAT'  // frontend alias pour priority
  priority?: string         // backend: priority
  orderedBy?: string        // backend: orderedBy
  orderedById?: string
  requestedBy?: string      // alias
  requestedByName?: string
  status: LabOrderStatus
  result?: string
  resultUnit?: string
  referenceRange?: string
  interpretation?: ResultInterpretation
  resultDate?: string
  resultComment?: string
  orderedAt: string
}

// ─── Tenant ───────────────────────────────────────────────────────────────────

export interface Tenant {
  id: string
  name: string
  domain?: string
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  contactEmail?: string
  contactPhone?: string
  planId?: string
  planName?: string
  subscriptionStatus?: string
  createdAt: string
  updatedAt?: string
}

