import { useQuery } from '@tanstack/react-query'
import { BedDouble, RefreshCw } from 'lucide-react'
import { Card, CardHeader, CardBody, Badge, Spinner, EmptyState, Button } from '@/components/ui'
import { gapEncounterService } from '@/services/gap.service'
import { format } from 'date-fns'
import type { BedBoardEntry } from '@/types'

function getBedColor(status: string) {
  switch (status) {
    case 'OCCUPIED': return 'border-clinical-danger bg-clinical-danger-bg'
    case 'AVAILABLE': return 'border-clinical-success bg-clinical-success-bg'
    case 'CLEANING': return 'border-clinical-warning bg-clinical-warning-bg'
    default: return 'border-surface-border bg-slate-50'
  }
}

function getBedVariant(status: string): 'danger' | 'normal' | 'warning' | 'neutral' {
  switch (status) {
    case 'OCCUPIED': return 'danger'
    case 'AVAILABLE': return 'normal'
    case 'CLEANING': return 'warning'
    default: return 'neutral'
  }
}

function getBedLabel(status: string): string {
  return { OCCUPIED: 'Occupé', AVAILABLE: 'Disponible', CLEANING: 'Nettoyage', MAINTENANCE: 'Maintenance' }[status] ?? status
}

export default function BedBoardPage() {
  const { data: beds, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['bed-board'],
    queryFn: gapEncounterService.getBedBoard,
    refetchInterval: 60_000,
  })

  // Grouper par unité
  const byWard = (beds ?? []).reduce<Record<string, BedBoardEntry[]>>((acc, bed) => {
    const ward = bed.ward || 'Non assigné'
    if (!acc[ward]) acc[ward] = []
    acc[ward].push(bed)
    return acc
  }, {})

  const occupied = beds?.filter((b) => b.bedStatus === 'OCCUPIED').length ?? 0
  const available = beds?.filter((b) => b.bedStatus === 'AVAILABLE').length ?? 0
  const total = beds?.length ?? 0

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-text-primary">Tableau des lits</h1>
          <p className="text-sm text-text-muted mt-0.5">
            {total > 0
              ? `${occupied} occupé(s) · ${available} disponible(s) · ${total} au total`
              : 'Vue en temps réel de l\'occupation des lits'}
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          icon={<RefreshCw className={`h-4 w-4 ${isFetching ? 'animate-spin' : ''}`} />}
          onClick={() => refetch()}
        >
          Actualiser
        </Button>
      </div>

      {/* Légende */}
      <div className="flex flex-wrap gap-3">
        {[
          { status: 'OCCUPIED', label: 'Occupé' },
          { status: 'AVAILABLE', label: 'Disponible' },
          { status: 'CLEANING', label: 'Nettoyage' },
          { status: 'MAINTENANCE', label: 'Maintenance' },
        ].map(({ status, label }) => (
          <div key={status} className="flex items-center gap-1.5">
            <div className={`h-3 w-3 rounded-sm border ${getBedColor(status)}`} />
            <span className="text-xs text-text-muted">{label}</span>
          </div>
        ))}
      </div>

      {isLoading ? (
        <div className="flex justify-center py-16"><Spinner size="lg" /></div>
      ) : !beds?.length ? (
        <EmptyState
          icon={<BedDouble className="h-12 w-12" />}
          title="Aucun lit enregistré"
          description="Les lits apparaissent ici après la première admission."
        />
      ) : (
        <div className="space-y-6">
          {Object.entries(byWard).map(([ward, wardBeds]) => (
            <Card key={ward}>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <h2 className="text-sm font-semibold text-text-primary">{ward}</h2>
                  <span className="text-xs text-text-muted">
                    {wardBeds.filter((b) => b.bedStatus === 'OCCUPIED').length} / {wardBeds.length} occupé(s)
                  </span>
                </div>
              </CardHeader>
              <CardBody>
                <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
                  {wardBeds.map((bed) => (
                    <div
                      key={bed.encounterId}
                      className={`rounded-lg border-2 p-3 transition-all ${getBedColor(bed.bedStatus)}`}
                    >
                      <div className="flex items-start justify-between mb-2">
                        <div>
                          <p className="text-xs font-bold text-text-primary">
                            {bed.room} / {bed.bed}
                          </p>
                        </div>
                        <Badge variant={getBedVariant(bed.bedStatus)} className="text-2xs">
                          {getBedLabel(bed.bedStatus)}
                        </Badge>
                      </div>
                      {bed.bedStatus === 'OCCUPIED' && (
                        <div>
                          <p className="text-xs font-medium text-text-primary truncate">{bed.patientName}</p>
                          <p className="text-2xs text-text-muted font-mono">{bed.mrn}</p>
                          <p className="text-2xs text-text-muted mt-1">
                            Depuis {format(new Date(bed.admissionDate), 'dd/MM')}
                          </p>
                          {bed.admittingDiagnosis && (
                            <p className="text-2xs text-text-muted truncate mt-0.5" title={bed.admittingDiagnosis}>
                              {bed.admittingDiagnosis}
                            </p>
                          )}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
