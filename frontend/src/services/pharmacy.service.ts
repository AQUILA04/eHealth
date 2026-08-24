import apiClient from '@/lib/axios'
import type { Dispensation, InventoryLot, MedicationProduct } from '@/types'

export const pharmacyService = {
  listProducts: () => apiClient.get<MedicationProduct[]>('/pharmacy/products').then((response) => response.data),
  createProduct: (data: { sku: string; name: string; genericName?: string; atcCode?: string; unit: string; minimumStock: number }) =>
    apiClient.post<MedicationProduct>('/pharmacy/products', data).then((response) => response.data),
  listLots: (productId?: number) => apiClient.get<InventoryLot[]>('/pharmacy/inventory/lots', { params: { productId } }).then((response) => response.data),
  receiveLot: (data: { productId: number; lotNumber: string; quantity: number; expiryDate: string; storageLocation: string; supplier?: string }) =>
    apiClient.post<InventoryLot>('/pharmacy/inventory/receipts', data).then((response) => response.data),
  listDispensations: (patientRef?: string) => apiClient.get<Dispensation[]>('/pharmacy/dispensations', { params: { patientRef } }).then((response) => response.data),
  validateDispensation: (data: { clinicalEncounterId: number; patientRef: string; productId: number; quantity: number; pharmacist: string; clinicalPrescriptionRef?: string }) =>
    apiClient.post<Dispensation>('/pharmacy/dispensations', data).then((response) => response.data),
  dispense: (id: number, lotId?: number) => apiClient.patch<Dispensation>(`/pharmacy/dispensations/${id}/dispense`, { lotId }).then((response) => response.data),
}
