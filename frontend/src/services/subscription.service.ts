import apiClient from '@/lib/axios'

export interface PlanLimitsWindow {
  period: string
  limit: number | null
  enforce?: string
}

export interface PlanOperationLimit {
  type: 'capacity' | 'usage'
  windows: PlanLimitsWindow[]
}

export interface SubscriptionPlan {
  id: string
  name: string
  description?: string
  price: number
  currency: string
  billingInterval: string
  isPublic: boolean
  isActive: boolean
  isFree: boolean
  autoApproveSignups: boolean
  stripePriceId?: string
  sortOrder: number
  limits: Record<string, PlanOperationLimit | Record<string, unknown>>
  features: Record<string, boolean>
  createdAt?: string
}

export type SignupRequestStatus = 'PENDING' | 'COMPLETED' | 'REJECTED'

export interface SignupRequest {
  id: string
  organizationName: string
  subdomain?: string
  adminEmail: string
  adminFirstName: string
  adminLastName: string
  adminPhone?: string
  planId: string
  planName?: string
  planFree?: boolean
  status: SignupRequestStatus
  tenantId?: string
  reviewedBy?: string
  reviewedAt?: string
  rejectionReason?: string
  provisionError?: string
  createdAt: string
  updatedAt?: string
}

export interface SignupSubmitResult {
  requestId: string
  status: SignupRequestStatus
  provisioned: boolean
  tenantId?: string
  message: string
  temporaryPassword?: string
  createdAt?: string
}

interface WrappedResponse<T> {
  status: string
  statusCode: number
  message: string
  service: string
  data: T
}

export const subscriptionService = {
  listPublicPlans: () =>
    apiClient
      .get<WrappedResponse<SubscriptionPlan[]>>('/signup/plans')
      .then((r) => r.data.data),

  listPlans: () =>
    apiClient
      .get<WrappedResponse<SubscriptionPlan[]>>('/subscriptions/plans')
      .then((r) => r.data.data),

  createPlan: (data: Partial<SubscriptionPlan>) =>
    apiClient
      .post<WrappedResponse<SubscriptionPlan>>('/subscriptions/plans', data)
      .then((r) => r.data.data),

  updatePlan: (id: string, data: Partial<SubscriptionPlan>) =>
    apiClient
      .patch<WrappedResponse<SubscriptionPlan>>(`/subscriptions/plans/${id}`, data)
      .then((r) => r.data.data),
}

export const signupService = {
  submit: (data: {
    organizationName: string
    adminEmail: string
    adminFirstName: string
    adminLastName: string
    adminPhone?: string
    planId: string
    subdomain?: string
  }) =>
    apiClient
      .post<WrappedResponse<SignupSubmitResult>>('/signup', data)
      .then((r) => r.data.data),

  listRequests: (status?: SignupRequestStatus) =>
    apiClient
      .get<WrappedResponse<SignupRequest[]>>('/signup/requests', {
        params: status ? { status } : undefined,
      })
      .then((r) => r.data.data),

  approve: (id: string) =>
    apiClient
      .post<WrappedResponse<SignupSubmitResult>>(`/signup/requests/${id}/approve`)
      .then((r) => r.data.data),

  reject: (id: string, reason?: string) =>
    apiClient
      .post<WrappedResponse<SignupRequest>>(`/signup/requests/${id}/reject`, { reason })
      .then((r) => r.data.data),
}

export const OPERATION_REGISTRY = [
  {
    key: 'establishments.capacity',
    label: 'Établissements',
    description: 'Nombre de sites / établissements',
    type: 'capacity' as const,
    periods: ['none'],
  },
  {
    key: 'users.capacity',
    label: 'Utilisateurs',
    description: 'Comptes Keycloak du tenant',
    type: 'capacity' as const,
    periods: ['none'],
  },
  {
    key: 'patients.capacity',
    label: 'Patients',
    description: 'Patients enregistrés (GAP)',
    type: 'capacity' as const,
    periods: ['none'],
  },
  {
    key: 'encounters.create',
    label: 'Admissions',
    description: 'Créations d’encounters ADT',
    type: 'usage' as const,
    periods: ['monthly'],
  },
  {
    key: 'appointments.create',
    label: 'Rendez-vous',
    description: 'Créations de rendez-vous',
    type: 'usage' as const,
    periods: ['monthly'],
  },
]

export function ensurePlanLimits(
  limits: Record<string, unknown> | undefined
): Record<string, PlanOperationLimit> {
  const result: Record<string, PlanOperationLimit> = {}
  for (const op of OPERATION_REGISTRY) {
    const existing = limits?.[op.key] as PlanOperationLimit | undefined
    if (existing?.windows?.length) {
      result[op.key] = existing as PlanOperationLimit
    } else {
      result[op.key] = {
        type: op.type,
        windows: op.periods.map((period) => ({
          period,
          limit: op.type === 'capacity' ? 1 : 30,
          enforce: 'hard',
        })),
      }
    }
  }
  return result
}
