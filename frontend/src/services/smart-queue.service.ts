import apiClient from '@/lib/axios'
export type Acuity='P1_IMMEDIATE'|'P2_VERY_URGENT'|'P3_URGENT'|'P4_STANDARD'|'P5_ROUTINE'
export interface QueueTicket { id:number; ticketNumber:string; serviceArea:string; acuity:Acuity; status:'WAITING'|'CALLED'|'IN_CONSULTATION'|'COMPLETED'|'CANCELLED'; patientName:string; checkedInAt:string; calledAt?:string }
export interface PublicTicket { ticketNumber:string; serviceArea:string; status:string; calledAt:string }
export interface QueueView { waiting:QueueTicket[]; nextCandidate?:QueueTicket; recentlyCalled:PublicTicket[] }
export const smartQueueService={queue:(serviceArea:string)=>apiClient.get<QueueView>('/gap/queue',{params:{serviceArea}}).then(r=>r.data),callNext:(serviceArea:string,clinicianId:string)=>apiClient.post<QueueTicket>('/gap/queue/next',null,{params:{serviceArea,clinicianId}}).then(r=>r.data),display:(serviceArea:string)=>apiClient.get<PublicTicket[]>('/gap/queue/display',{params:{serviceArea}}).then(r=>r.data)}
