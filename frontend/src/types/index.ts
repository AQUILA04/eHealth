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

// ─── Module III — Banque de sang ────────────────────────────────────────────

export type BloodAboGroup = 'A' | 'B' | 'AB' | 'O'
export type BloodRhesus = 'POSITIVE' | 'NEGATIVE'
export type BloodComponent = 'RED_CELLS' | 'PLASMA' | 'PLATELETS'
export type BloodUnitStatus = 'AVAILABLE' | 'RESERVED' | 'ISSUED' | 'TRANSFUSED' | 'EXPIRED' | 'DISCARDED'
export type TransfusionStatus = 'REQUESTED' | 'COMPATIBILITY_VALIDATED' | 'ISSUED' | 'COMPLETED' | 'REACTION_REPORTED' | 'CANCELLED'

export interface BloodUnit {
  id: number
  donationCode: string
  aboGroup: BloodAboGroup
  rhesus: BloodRhesus
  component: BloodComponent
  collectedOn: string
  expiresOn: string
  status: BloodUnitStatus
  storageLocation?: string
  expiringSoon: boolean
}

export interface Transfusion {
  id: number
  clinicalEncounterId: number
  patientRef: string
  recipientAboGroup: BloodAboGroup
  recipientRhesus: BloodRhesus
  component: BloodComponent
  bloodUnitId: number
  donationCode: string
  donorAboGroup: BloodAboGroup
  donorRhesus: BloodRhesus
  status: TransfusionStatus
  requestedBy: string
  crossmatchValidatedBy?: string
  issuedBy?: string
  completedBy?: string
  requestedAt: string
  crossmatchValidatedAt?: string
  issuedAt?: string
  completedAt?: string
  reactionDescription?: string
  reactionReportedAt?: string
}


// ─── Module III — LIS / Laboratoire ─────────────────────────────────────────

export type LaboratoryPriority = 'ROUTINE' | 'URGENT' | 'STAT'
export type LaboratoryStatus = 'ORDERED' | 'COLLECTED' | 'RECEIVED' | 'IN_ANALYSIS' | 'TECHNICALLY_VALIDATED' | 'BIOLOGICALLY_VALIDATED' | 'CANCELLED'
export type LaboratoryInterpretation = 'NORMAL' | 'LOW' | 'HIGH' | 'ABNORMAL' | 'CRITICAL_LOW' | 'CRITICAL_HIGH'

export interface LaboratoryResult {
  id: number
  analyteName: string
  analyteCode?: string
  resultValue: string
  unit?: string
  referenceRange?: string
  interpretation: LaboratoryInterpretation
  technicalValidator?: string
  resultedAt: string
}

export interface LaboratoryOrder {
  id: number
  clinicalEncounterId: number
  patientRef: string
  examName: string
  examCode?: string
  sampleType: string
  barcode?: string
  priority: LaboratoryPriority
  status: LaboratoryStatus
  orderedBy?: string
  collectedBy?: string
  receivedBy?: string
  validatedBy?: string
  orderedAt: string
  collectedAt?: string
  receivedAt?: string
  validatedAt?: string
  criticalNotifiedAt?: string
  criticalNotifiedTo?: string
  results: LaboratoryResult[]
}

// ─── Module III — RIS / Radiologie ──────────────────────────────────────────

export type RadiologyModality = 'XR' | 'CT' | 'MRI' | 'US' | 'NM' | 'MAMMO' | 'OTHER'
export type RadiologyPriority = 'ROUTINE' | 'URGENT' | 'STAT'
export type RadiologyStatus = 'REQUESTED' | 'SCHEDULED' | 'CHECKED_IN' | 'IN_PROGRESS' | 'COMPLETED' | 'REPORTED' | 'CANCELLED'

export interface RadiologyStudy {
  id: number
  clinicalEncounterId: number
  patientRef: string
  procedureName: string
  procedureCode?: string
  modality: RadiologyModality
  priority: RadiologyPriority
  status: RadiologyStatus
  requestedBy?: string
  assignedRadiologist?: string
  assignedTechnologist?: string
  pacsStudyUid?: string
  reportText?: string
  radiationDoseMgy?: number
  requestedAt: string
  scheduledAt?: string
  performedAt?: string
  reportedAt?: string
}

// ─── Module IV — Pharmacie et stocks ────────────────────────────────────────

export type DispensationStatus = 'VALIDATED' | 'DISPENSED' | 'CANCELLED'

export interface MedicationProduct {
  id: number
  sku: string
  name: string
  genericName?: string
  atcCode?: string
  unit: string
  minimumStock: number
  quantityOnHand: number
  lowStock: boolean
  active: boolean
}

export interface InventoryLot {
  id: number
  productId: number
  productName: string
  lotNumber: string
  quantityOnHand: number
  expiryDate: string
  storageLocation: string
  supplier?: string
  expiringSoon: boolean
}

export interface Dispensation {
  id: number
  clinicalEncounterId: number
  patientRef: string
  productId: number
  productName: string
  lotId?: number
  lotNumber?: string
  quantity: number
  status: DispensationStatus
  pharmacist: string
  clinicalPrescriptionRef?: string
  validatedAt: string
  dispensedAt?: string
}

// ─── Module V — Gestion financière et cycle de revenus ─────────────────────
export type InvoiceStatus = 'DRAFT' | 'ISSUED' | 'PARTIALLY_PAID' | 'PAID' | 'CANCELLED'
export type ClaimStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'PARTIALLY_APPROVED' | 'DENIED'
export interface InvoiceLine { id: number; serviceCode: string; description: string; quantity: number; unitPrice: number; lineTotal: number }
export interface Invoice { id: number; invoiceNumber: string; patientRef: string; clinicalEncounterId?: number; currency: string; payerType: string; insurerName?: string; status: InvoiceStatus; totalAmount: number; insurerAmount: number; patientAmount: number; outstandingAmount: number; createdAt: string; issuedAt?: string; lines: InvoiceLine[] }
export interface RcmPayment { id: number; invoiceId: number; amount: number; method: string; reference?: string; receivedAt: string; receivedBy: string }
export interface InsuranceClaim { id: number; claimNumber: string; invoiceId: number; insurerName: string; policyNumber: string; status: ClaimStatus; requestedAmount: number; approvedAmount: number; denialReason?: string; createdAt: string; submittedAt?: string; adjudicatedAt?: string }


// ─── Module VI — Ressources humaines et gestion du personnel ────────────────
export interface StaffMember { id: number; employeeNumber: string; firstName: string; lastName: string; department: string; position: string; employmentStatus: string; clinicalStaff: boolean; email?: string; hiredOn: string }
export interface Credential { id: number; staffId: number; staffName: string; type: string; credentialNumber: string; issuedOn: string; expiresOn: string; expired: boolean }
export type ShiftStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED'
export interface ShiftAssignment { id: number; staffId: number; staffName: string; unitName: string; shiftType: string; startsAt: string; endsAt: string; status: ShiftStatus; notes?: string }


// ─── Module VII — Services de support et hôtellerie ─────────────────────────
export interface MealOrder { id: number; patientRef: string; dietCode: string; mealType: string; scheduledOn: string; bedRef?: string; status: 'REQUESTED' | 'PREPARED' | 'DELIVERED'; deliveredAt?: string }
export interface EquipmentAsset { id: number; assetTag: string; name: string; location: string; critical: boolean; status: 'OPERATIONAL' | 'MAINTENANCE'; lastMaintenance?: string }
export interface MaintenanceOrder { id: number; equipmentId: number; equipmentName: string; title: string; type: string; dueOn: string; status: 'OPEN' | 'COMPLETED'; assignedTo?: string; completedAt?: string }
export interface CleaningTask { id: number; bedRef: string; unitName: string; cleaningType: string; status: 'REQUESTED' | 'COMPLETED'; requestedAt: string; completedAt?: string }


// ─── Module VIII — Engagement patient et télémédecine ───────────────────────
export interface PortalAppointment { id: number; patientRef: string; practitioner: string; specialty: string; scheduledAt: string; status: 'REQUESTED' | 'CONFIRMED' }
export interface Teleconsultation { id: number; patientRef: string; practitioner: string; scheduledAt: string; meetingRoom: string; status: 'SCHEDULED' | 'STARTED' | 'COMPLETED' }
