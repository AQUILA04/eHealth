import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CalendarDays, Plus, ChevronLeft, ChevronRight } from 'lucide-react'
import {
  Button, Badge, Card, CardHeader, CardBody,
  Spinner, EmptyState, Table, Thead, Th, Tr, Td, Modal, Input, Select
} from '@/components/ui'
import { gapAppointmentService, gapPatientService } from '@/services/gap.service'
import { format, startOfDay, endOfDay, addDays, subDays } from 'date-fns'
import { fr } from 'date-fns/locale'
import type { Appointment, AppointmentStatus } from '@/types'

const STATUS_LABELS: Record<string, string> = {
  SCHEDULED: 'Planifié', CONFIRMED: 'Confirmé', CHECKED_IN: 'Arrivé',
  COMPLETED: 'Terminé', CANCELLED: 'Annulé', NO_SHOW: 'Absent',
}
const STATUS_VARIANTS: Record<string, 'normal' | 'info' | 'warning' | 'danger' | 'neutral' | 'primary'> = {
  SCHEDULED: 'neutral', CONFIRMED: 'normal', CHECKED_IN: 'info',
  COMPLETED: 'primary', CANCELLED: 'danger', NO_SHOW: 'warning',
}

const SPECIALTIES = [
  'Médecine générale', 'Cardiologie', 'Neurologie', 'Pédiatrie',
  'Chirurgie générale', 'Gynécologie', 'Ophtalmologie', 'Dermatologie',
  'Radiologie', 'Biologie médicale', 'Urgences',
]

export default function AppointmentsPage() {
  const qc = useQueryClient()
  const [selectedDate, setSelectedDate] = useState(new Date())
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState<Partial<Appointment & { patientMrn: string }>>({
    durationMinutes: 30,
    specialty: 'Médecine générale',
  })

  const { data: appointments, isLoading } = useQuery({
    queryKey: ['appointments', format(selectedDate, 'yyyy-MM-dd')],
    queryFn: () =>
      gapAppointmentService.getByPeriod(
        startOfDay(selectedDate).toISOString(),
        endOfDay(selectedDate).toISOString()
      ),
    staleTime: 30_000,
  })

  const createMutation = useMutation({
    mutationFn: async (data: Partial<Appointment & { patientMrn: string }>) => {
      const patient = await gapPatientService.getByMrn(data.patientMrn!)
      return gapAppointmentService.create({
        ...data,
        patientId: patient.id,
      })
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['appointments'] })
      setShowCreate(false)
      setForm({ durationMinutes: 30, specialty: 'Médecine générale' })
    },
  })

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      gapAppointmentService.updateStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['appointments'] }),
  })

  const counts = {
    total: appointments?.length ?? 0,
    confirmed: appointments?.filter((a) => a.status === 'CONFIRMED').length ?? 0,
    checkedIn: appointments?.filter((a) => a.status === 'CHECKED_IN').length ?? 0,
    completed: appointments?.filter((a) => a.status === 'COMPLETED').length ?? 0,
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-text-primary">Rendez-vous</h1>
          <p className="text-sm text-text-muted mt-0.5">
            {format(selectedDate, "EEEE d MMMM yyyy", { locale: fr })}
          </p>
        </div>
        <Button icon={<Plus className="h-4 w-4" />} onClick={() => setShowCreate(true)}>
          Nouveau RDV
        </Button>
      </div>

      {/* Navigation date */}
      <div className="flex items-center gap-3">
        <Button
          variant="outline"
          size="sm"
          icon={<ChevronLeft className="h-4 w-4" />}
          onClick={() => setSelectedDate((d) => subDays(d, 1))}
        />
        <button
          className="text-sm font-medium text-primary-600 hover:underline cursor-pointer"
          onClick={() => setSelectedDate(new Date())}
        >
          Aujourd'hui
        </button>
        <Button
          variant="outline"
          size="sm"
          icon={<ChevronRight className="h-4 w-4" />}
          onClick={() => setSelectedDate((d) => addDays(d, 1))}
        />
        <input
          type="date"
          className="ml-2 rounded-lg border border-surface-border px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
          value={format(selectedDate, 'yyyy-MM-dd')}
          onChange={(e) => setSelectedDate(new Date(e.target.value))}
        />
      </div>

      {/* Stats du jour */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {[
          { label: 'Total', value: counts.total, color: 'bg-primary-50 text-primary-700' },
          { label: 'Confirmés', value: counts.confirmed, color: 'bg-clinical-success-bg text-clinical-success' },
          { label: 'Arrivés', value: counts.checkedIn, color: 'bg-clinical-info-bg text-clinical-info' },
          { label: 'Terminés', value: counts.completed, color: 'bg-slate-100 text-slate-600' },
        ].map(({ label, value, color }) => (
          <Card key={label} className="p-4">
            <p className="text-2xl font-bold text-text-primary">{value}</p>
            <p className="text-xs text-text-muted mt-0.5">{label}</p>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <h2 className="text-sm font-semibold text-text-primary flex items-center gap-2">
            <CalendarDays className="h-4 w-4 text-primary-600" />
            Liste des rendez-vous
          </h2>
        </CardHeader>
        <CardBody className="p-0">
          {isLoading ? (
            <div className="flex justify-center py-12"><Spinner /></div>
          ) : !appointments?.length ? (
            <EmptyState
              icon={<CalendarDays className="h-10 w-10" />}
              title="Aucun rendez-vous ce jour"
              description="Planifiez un rendez-vous pour ce jour."
              action={
                <Button size="sm" onClick={() => setShowCreate(true)} icon={<Plus className="h-4 w-4" />}>
                  Nouveau RDV
                </Button>
              }
            />
          ) : (
            <Table>
              <Thead>
                <tr>
                  <Th>Heure</Th>
                  <Th>Patient</Th>
                  <Th>Spécialité</Th>
                  <Th>Praticien</Th>
                  <Th>Durée</Th>
                  <Th>Statut</Th>
                  <Th>Actions</Th>
                </tr>
              </Thead>
              <tbody>
                {(appointments as Appointment[])
                  .sort((a, b) => new Date(a.scheduledTime).getTime() - new Date(b.scheduledTime).getTime())
                  .map((appt) => (
                    <Tr key={appt.id}>
                      <Td>
                        <span className="font-mono text-sm font-semibold text-primary-700">
                          {format(new Date(appt.scheduledTime), 'HH:mm')}
                        </span>
                      </Td>
                      <Td>
                        <div>
                          <p className="font-medium text-text-primary">{appt.patientFullName || appt.patientMrn}</p>
                          <p className="text-xs text-text-muted font-mono">{appt.patientMrn}</p>
                        </div>
                      </Td>
                      <Td className="text-sm">{appt.specialty}</Td>
                      <Td className="text-sm">{appt.practitionerName || <span className="text-text-muted">—</span>}</Td>
                      <Td className="text-sm">{appt.durationMinutes} min</Td>
                      <Td>
                        <Badge variant={STATUS_VARIANTS[appt.status]}>
                          {STATUS_LABELS[appt.status]}
                        </Badge>
                      </Td>
                      <Td>
                        <div className="flex gap-1">
                          {appt.status === 'SCHEDULED' && (
                            <Button
                              size="sm"
                              variant="secondary"
                              onClick={() => updateStatusMutation.mutate({ id: appt.id, status: 'CHECKED_IN' })}
                            >
                              Arrivée
                            </Button>
                          )}
                          {appt.status === 'CHECKED_IN' && (
                            <Button
                              size="sm"
                              variant="secondary"
                              onClick={() => updateStatusMutation.mutate({ id: appt.id, status: 'COMPLETED' })}
                            >
                              Terminer
                            </Button>
                          )}
                          {['SCHEDULED', 'CONFIRMED'].includes(appt.status) && (
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={() => updateStatusMutation.mutate({ id: appt.id, status: 'CANCELLED' })}
                            >
                              Annuler
                            </Button>
                          )}
                        </div>
                      </Td>
                    </Tr>
                  ))}
              </tbody>
            </Table>
          )}
        </CardBody>
      </Card>

      {/* Modal création RDV */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Nouveau rendez-vous" size="lg">
        <form
          onSubmit={(e) => { e.preventDefault(); createMutation.mutate(form) }}
          className="space-y-4"
        >
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="MRN du patient *"
              required
              placeholder="ex: MRN-00001"
              value={(form as Record<string, string>).patientMrn || ''}
              onChange={(e) => setForm({ ...form, patientMrn: e.target.value } as typeof form)}
            />
            <Input
              label="Date et heure *"
              type="datetime-local"
              required
              value={form.scheduledTime || ''}
              onChange={(e) => setForm({ ...form, scheduledTime: e.target.value })}
            />
            <Select
              label="Spécialité *"
              value={form.specialty || 'Médecine générale'}
              onChange={(e) => setForm({ ...form, specialty: e.target.value })}
              options={SPECIALTIES.map((s) => ({ value: s, label: s }))}
            />
            <Input
              label="Durée (minutes)"
              type="number"
              min={5}
              max={240}
              value={form.durationMinutes || 30}
              onChange={(e) => setForm({ ...form, durationMinutes: parseInt(e.target.value) })}
            />
            <Input
              label="Praticien"
              value={form.practitionerName || ''}
              onChange={(e) => setForm({ ...form, practitionerName: e.target.value })}
            />
            <Input
              label="Salle / Cabinet"
              value={form.room || ''}
              onChange={(e) => setForm({ ...form, room: e.target.value })}
            />
          </div>
          <Input
            label="Motif du rendez-vous"
            value={form.reason || ''}
            onChange={(e) => setForm({ ...form, reason: e.target.value })}
          />
          {createMutation.isError && (
            <p className="text-sm text-clinical-danger">Erreur lors de la création du rendez-vous.</p>
          )}
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowCreate(false)}>Annuler</Button>
            <Button type="submit" loading={createMutation.isPending}>Planifier le RDV</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
