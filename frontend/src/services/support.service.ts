import apiClient from '@/lib/axios'
import type { CleaningTask, EquipmentAsset, MealOrder, MaintenanceOrder } from '@/types'
export const supportService = {
  meals: () => apiClient.get<MealOrder[]>('/support/meals').then(r => r.data),
  createMeal: (data: { patientRef: string; dietCode: string; mealType: string; scheduledOn: string; bedRef?: string }) => apiClient.post<MealOrder>('/support/meals', data).then(r => r.data),
  prepareMeal: (id: number) => apiClient.post<MealOrder>(`/support/meals/${id}/prepare`).then(r => r.data),
  deliverMeal: (id: number) => apiClient.post<MealOrder>(`/support/meals/${id}/deliver`).then(r => r.data),
  equipment: () => apiClient.get<EquipmentAsset[]>('/support/equipment').then(r => r.data),
  maintenance: () => apiClient.get<MaintenanceOrder[]>('/support/maintenance').then(r => r.data),
  completeMaintenance: (id: number) => apiClient.post<MaintenanceOrder>(`/support/maintenance/${id}/complete`).then(r => r.data),
  cleaning: () => apiClient.get<CleaningTask[]>('/support/cleaning').then(r => r.data),
  completeCleaning: (id: number) => apiClient.post<CleaningTask>(`/support/cleaning/${id}/complete`).then(r => r.data),
}
