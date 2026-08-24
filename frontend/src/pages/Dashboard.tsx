import { useQuery } from '@tanstack/react-query'
import { BedDouble, CalendarDays, AlertTriangle, Clock } from 'lucide-react'
import { StatCard, Card, CardHeader, CardBody, Badge, Spinner, Table, Thead, Th, Tr, Td } from '@/components/ui'
import { gapEncounterService, gapAppointmentService } from '@/services/gap.service'
import { format, startOfDay, endOfDay } from 'date-fns'
import { fr } from 'date-fns/locale'
import type { BedBoardEntry, Appointment } from '@/types'

function getBedStatusVariant(status: string): 'danger' | 'normal' | 'warning' | 'neutral' {
  switch (status) {
    case 'OCCUPIED': return 'danger'
    case 'AVAILABLE': return 'normal'
    case 'CLEANING': return 'warning'
    default: return 'neutral'
  }
}

function getBedStatusLabel(status: string): string {
  const labels: Record<string, string> = {
    OCCUPIED: 'Occupé',
    AVAILABLE: 'Disponible',
    CLEANING: 'Nettoyage',
    MAINTENANCE: 'Maintenance',
  }
  return labels[status] || status
}

function getAppointmentVariant(status: string): 'normal' | 'warning' | 'danger' | 'info' | 'neutral' {
  switch (status) {
    case 'CONFIRMED': return 'normal'
    case 'CHECKED_IN': return 'info'
    case 'CANCELLED': return 'danger'
    case 'NO_SHOW': return 'warning'
    default: return 'neutral'
  }
}

function getAppointmentLabel(status: string): string {
  const labels: Record<string, string> = {
    SCHEDULED: 'Planifié',
    CONFIRMED: 'Confirmé',
    CHECKED_IN: 'Arrivé',
    COMPLETED: 'Terminé',
    CANCELLED: 'Annulé',
    NO_SHOW: 'Absent',
  }
  return labels[status] || status
}

export default function Dashboard() {
  const today = new Date()
  const { data: bedBoard, isLoading: bedLoading } = useQuery({
    queryKey: ['bed-board'],
    queryFn: gapEncounterService.getBedBoard,
    refetchInterval: 30_000,
  })

  const { data: appointments, isLoading: apptLoading } = useQuery({
    queryKey: ['appointments-today'],
    queryFn: () =>
      gapAppointmentService.getByPeriod(
        startOfDay(today).toISOString(),
        endOfDay(today).toISOString()
      ),
  })

  const occupied = bedBoard?.filter((b) => b.bedStatus === 'OCCUPIED').length ?? 0
  const available = bedBoard?.filter((b) => b.bedStatus === 'AVAILABLE').length ?? 0
  const totalBeds = bedBoard?.length ?? 0
  const todayAppts = appointments?.length ?? 0
  const pendingAppts = appointments?.filter((a) => a.status === 'SCHEDULED').length ?? 0

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-text-primary">Vue d'ensemble</h1>
        <p className="text-sm text-text-muted mt-0.5">
          {format(today, "EEEE d MMMM yyyy", { locale: fr })}
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard
          label="Lits occupés"
          value={bedLoading ? '—' : occupied}
          icon={<BedDouble className="h-5 w-5" />}
          color="danger"
        />
        <StatCard
          label="Lits disponibles"
          value={bedLoading ? '—' : available}
          icon={<BedDouble className="h-5 w-5" />}
          color="success"
        />
        <StatCard
          label="RDV aujourd'hui"
          value={apptLoading ? '—' : todayAppts}
          icon={<CalendarDays className="h-5 w-5" />}
          color="primary"
        />
        <StatCard
          label="RDV en attente"
          value={apptLoading ? '—' : pendingAppts}
          icon={<Clock className="h-5 w-5" />}
          color="warning"
        />
      </div>

      {/* Occupation des lits */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold text-text-primary flex items-center gap-2">
                <BedDouble className="h-4 w-4 text-primary-600" />
                Tableau des lits — Résumé
              </h2>
              {totalBeds > 0 && (
                <span className="text-xs text-text-muted">
                  {Math.round((occupied / totalBeds) * 100)}% d'occupation
                </span>
              )}
            </div>
          </CardHeader>
          <CardBody className="p-0">
            {bedLoading ? (
              <div className="flex justify-center py-8">
                <Spinner />
              </div>
            ) : !bedBoard?.length ? (
              <div className="flex flex-col items-center py-8 text-text-muted">
                <BedDouble className="h-8 w-8 mb-2" />
                <p className="text-sm">Aucun lit enregistré</p>
              </div>
            ) : (
              <Table>
                <Thead>
                  <tr>
                    <Th>Patient</Th>
                    <Th>Unité / Lit</Th>
                    <Th>Statut</Th>
                    <Th>Depuis</Th>
                  </tr>
                </Thead>
                <tbody>
                  {(bedBoard as BedBoardEntry[]).slice(0, 6).map((entry) => (
                    <Tr key={entry.encounterId}>
                      <Td>
                        <div>
                          <p className="font-medium text-text-primary">{entry.patientName}</p>
                          <p className="text-xs text-text-muted">{entry.mrn}</p>
                        </div>
                      </Td>
                      <Td>
                        <span className="font-mono text-xs">{entry.ward} / {entry.bed}</span>
                      </Td>
                      <Td>
                        <Badge variant={getBedStatusVariant(entry.bedStatus)}>
                          {getBedStatusLabel(entry.bedStatus)}
                        </Badge>
                      </Td>
                      <Td className="text-xs text-text-muted">
                        {format(new Date(entry.admissionDate), 'dd/MM/yyyy')}
                      </Td>
                    </Tr>
                  ))}
                </tbody>
              </Table>
            )}
          </CardBody>
        </Card>

        {/* RDV du jour */}
        <Card>
          <CardHeader>
            <h2 className="text-sm font-semibold text-text-primary flex items-center gap-2">
              <CalendarDays className="h-4 w-4 text-primary-600" />
              Rendez-vous du jour
            </h2>
          </CardHeader>
          <CardBody className="p-0">
            {apptLoading ? (
              <div className="flex justify-center py-8">
                <Spinner />
              </div>
            ) : !appointments?.length ? (
              <div className="flex flex-col items-center py-8 text-text-muted">
                <CalendarDays className="h-8 w-8 mb-2" />
                <p className="text-sm">Aucun rendez-vous aujourd'hui</p>
              </div>
            ) : (
              <Table>
                <Thead>
                  <tr>
                    <Th>Heure</Th>
                    <Th>Patient</Th>
                    <Th>Spécialité</Th>
                    <Th>Statut</Th>
                  </tr>
                </Thead>
                <tbody>
                  {(appointments as Appointment[]).slice(0, 6).map((appt) => (
                    <Tr key={appt.id}>
                      <Td className="font-mono text-xs font-medium">
                        {format(new Date(appt.scheduledTime), 'HH:mm')}
                      </Td>
                      <Td>
                        <div>
                          <p className="font-medium text-text-primary">{appt.patientFullName || appt.patientMrn}</p>
                          <p className="text-xs text-text-muted">{appt.specialty || '—'}</p>
                        </div>
                      </Td>
                      <Td className="text-xs">{appt.specialty}</Td>
                      <Td>
                        <Badge variant={getAppointmentVariant(appt.status)}>
                          {getAppointmentLabel(appt.status)}
                        </Badge>
                      </Td>
                    </Tr>
                  ))}
                </tbody>
              </Table>
            )}
          </CardBody>
        </Card>
      </div>

      {/* Alertes */}
      <Card className="border-clinical-warning bg-clinical-warning-bg">
        <CardBody className="py-3">
          <div className="flex items-center gap-3">
            <AlertTriangle className="h-5 w-5 text-clinical-warning flex-shrink-0" />
            <div>
              <p className="text-sm font-medium text-clinical-warning">Mode développement actif</p>
              <p className="text-xs text-amber-700 mt-0.5">
                Les services EMPI, GAP et DPI utilisent le profil <code className="font-mono bg-amber-100 px-1 rounded">mock</code> avec une base H2 in-memory.
                Les données sont réinitialisées à chaque redémarrage.
              </p>
            </div>
          </div>
        </CardBody>
      </Card>
    </div>
  )
}
