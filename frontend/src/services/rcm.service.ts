import apiClient from '@/lib/axios'
import type { InsuranceClaim, Invoice, RcmPayment } from '@/types'

export const rcmService = {
  listInvoices: () => apiClient.get<Invoice[]>('/rcm/invoices').then((r) => r.data),
  createInvoice: (data: { patientRef: string; clinicalEncounterId?: number; currency?: string; payerType: string; insurerName?: string; coveragePercent?: number; lines: { serviceCode: string; description: string; quantity: number; unitPrice: number }[] }) => apiClient.post<Invoice>('/rcm/invoices', data).then((r) => r.data),
  issueInvoice: (id: number) => apiClient.post<Invoice>(`/rcm/invoices/${id}/issue`).then((r) => r.data),
  listPayments: (id: number) => apiClient.get<RcmPayment[]>(`/rcm/invoices/${id}/payments`).then((r) => r.data),
  recordPayment: (id: number, data: { amount: number; method: string; reference?: string; receivedBy: string }) => apiClient.post<RcmPayment>(`/rcm/invoices/${id}/payments`, data).then((r) => r.data),
  listClaims: () => apiClient.get<InsuranceClaim[]>('/rcm/claims').then((r) => r.data),
  createClaim: (data: { invoiceId: number; insurerName: string; policyNumber: string }) => apiClient.post<InsuranceClaim>('/rcm/claims', data).then((r) => r.data),
  submitClaim: (id: number) => apiClient.post<InsuranceClaim>(`/rcm/claims/${id}/submit`).then((r) => r.data),
  adjudicateClaim: (id: number, data: { status: 'APPROVED' | 'PARTIALLY_APPROVED' | 'DENIED'; approvedAmount: number; denialReason?: string }) => apiClient.post<InsuranceClaim>(`/rcm/claims/${id}/adjudicate`, data).then((r) => r.data),
}
