import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { BedDouble, RefreshCw, Plus, Trash2, Settings, AlertTriangle } from 'lucide-react'
import { Card, CardHeader, CardBody, Badge, Spinner, EmptyState, Button, Modal, Input, Select } from '@/components/ui'
import { gapEncounterService } from '@/services/gap.service'
import { format } from 'date-fns'
import type { BedBoardEntry } from '@/types'

interface ConfiguredBed {
  id: string
  ward: string
  room: string
  bed: string
  status: 'AVAILABLE' | 'CLEANING' | 'MAINTENANCE'
}

const DEFAULT_BEDS: ConfiguredBed[] = [
  { id: '1', ward: 'Médecine générale', room: 'Chambre 101', bed: 'Lit A', status: 'AVAILABLE' },
  { id: '2', ward: 'Médecine générale', room: 'Chambre 101', bed: 'Lit B', status: 'AVAILABLE' },
  { id: '3', ward: 'Médecine générale', room: 'Chambre 102', bed: 'Lit A', status: 'AVAILABLE' },
  { id: '4', ward: 'Pédiatrie', room: 'Chambre 201', bed: 'Lit A', status: 'AVAILABLE' },
  { id: '5', ward: 'Pédiatrie', room: 'Chambre 201', bed: 'Lit B', status: 'CLEANING' },
  { id: '6', ward: 'Cardiologie', room: 'Chambre 301', bed: 'Lit A', status: 'MAINTENANCE' },
]

function getBedColor(status: string) {
  switch (status) {
    case 'OCCUPIED': return 'border-clinical-danger bg-clinical-danger-bg'
    case 'AVAILABLE': return 'border-clinical-success bg-clinical-success-bg'
    case 'CLEANING': return 'border-clinical-warning bg-clinical-warning-bg'
    case 'MAINTENANCE': return 'border-slate-300 bg-slate-100/50'
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
  const [configuredBeds, setConfiguredBeds] = useState<ConfiguredBed[]>([])
  const [showConfigModal, setShowConfigModal] = useState(false)
  const [newBedForm, setNewBedForm] = useState<Partial<ConfiguredBed>>({
    ward: 'Médecine générale',
    status: 'AVAILABLE'
  })

  // Charger la configuration au démarrage
  useEffect(() => {
    const saved = localStorage.getItem('medical_center_beds')
    if (saved) {
      try {
        setConfiguredBeds(JSON.parse(saved))
      } catch (e) {
        setConfiguredBeds(DEFAULT_BEDS)
      }
    } else {
      setConfiguredBeds(DEFAULT_BEDS)
      localStorage.setItem('medical_center_beds', JSON.stringify(DEFAULT_BEDS))
    }
  }, [])

  const saveBeds = (beds: ConfiguredBed[]) => {
    setConfiguredBeds(beds)
    localStorage.setItem('medical_center_beds', JSON.stringify(beds))
  }

  const { data: activeAdmissions, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['bed-board'],
    queryFn: gapEncounterService.getBedBoard,
    refetchInterval: 30_000,
  })

  // Fusionner les lits configurés et les admissions actives
  const mergedBeds: BedBoardEntry[] = configuredBeds.map(cb => {
    // Chercher une admission active sur ce lit
    const admission = (activeAdmissions ?? []).find(adm => 
      adm.ward?.toLowerCase().trim() === cb.ward.toLowerCase().trim() &&
      adm.room?.toLowerCase().trim() === cb.room.toLowerCase().trim() &&
      adm.bed?.toLowerCase().trim() === cb.bed.toLowerCase().trim()
    )

    if (admission) {
      return {
        ...admission,
        bedStatus: 'OCCUPIED'
      }
    }

    return {
      encounterId: -(parseInt(cb.id) || Math.random() * 100000), // ID négatif temporaire pour lits vides
      patientName: '',
      mrn: '',
      ward: cb.ward,
      room: cb.room,
      bed: cb.bed,
      bedStatus: cb.status,
      admissionDate: '',
      admittingDiagnosis: ''
    }
  })

  // Récupérer les admissions actives qui ne sont pas associées à un lit configuré (pour éviter de masquer des données réelles)
  const unmatchedAdmissions = (activeAdmissions ?? []).filter(adm => {
    return !configuredBeds.some(cb => 
      adm.ward?.toLowerCase().trim() === cb.ward.toLowerCase().trim() &&
      adm.room?.toLowerCase().trim() === cb.room.toLowerCase().trim() &&
      adm.bed?.toLowerCase().trim() === cb.bed.toLowerCase().trim()
    )
  }).map(adm => ({ ...adm, bedStatus: 'OCCUPIED' as const }))

  const handleAddBed = (e: React.FormEvent) => {
    e.preventDefault()
    if (!newBedForm.ward || !newBedForm.room || !newBedForm.bed) return
    const newBed: ConfiguredBed = {
      id: Date.now().toString(),
      ward: newBedForm.ward,
      room: newBedForm.room,
      bed: newBedForm.bed,
      status: newBedForm.status as ConfiguredBed['status'] || 'AVAILABLE'
    }
    const updated = [...configuredBeds, newBed]
    saveBeds(updated)
    setNewBedForm({ ward: newBedForm.ward, status: 'AVAILABLE', room: '', bed: '' })
  }

  const handleDeleteBed = (id: string) => {
    const updated = configuredBeds.filter(b => b.id !== id)
    saveBeds(updated)
  }

  const handleUpdateBedStatus = (ward: string, room: string, bed: string, newStatus: ConfiguredBed['status']) => {
    const updated = configuredBeds.map(b => {
      if (b.ward === ward && b.room === room && b.bed === bed) {
        return { ...b, status: newStatus }
      }
      return b
    })
    saveBeds(updated)
  }

  // Grouper par unité
  const allDisplayBeds = [...mergedBeds, ...unmatchedAdmissions]
  const byWard = allDisplayBeds.reduce<Record<string, BedBoardEntry[]>>((acc, bed) => {
    const ward = bed.ward || 'Non assigné'
    if (!acc[ward]) acc[ward] = []
    acc[ward].push(bed)
    return acc
  }, {})

  const occupied = allDisplayBeds.filter((b) => b.bedStatus === 'OCCUPIED').length
  const available = allDisplayBeds.filter((b) => b.bedStatus === 'AVAILABLE').length
  const cleaning = allDisplayBeds.filter((b) => b.bedStatus === 'CLEANING').length
  const maintenance = allDisplayBeds.filter((b) => b.bedStatus === 'MAINTENANCE').length
  const total = allDisplayBeds.length

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-text-primary">Tableau des lits</h1>
          <p className="text-sm text-text-muted mt-0.5">
            {total > 0
              ? `${occupied} occupé(s) · ${available} disponible(s) · ${cleaning} nettoyage · ${maintenance} maintenance`
              : 'Vue en temps réel de l\'occupation des lits'}
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            icon={<Settings className="h-4 w-4" />}
            onClick={() => setShowConfigModal(true)}
          >
            Configurer les lits
          </Button>
          <Button
            variant="outline"
            size="sm"
            icon={<RefreshCw className={`h-4 w-4 ${isFetching ? 'animate-spin' : ''}`} />}
            onClick={() => refetch()}
          >
            Actualiser
          </Button>
        </div>
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
      ) : !allDisplayBeds.length ? (
        <EmptyState
          icon={<BedDouble className="h-12 w-12" />}
          title="Aucun lit enregistré"
          description="Utilisez le bouton Configurer les lits pour ajouter des lits au centre médical."
          action={
            <Button size="sm" onClick={() => setShowConfigModal(true)} icon={<Settings className="h-4 w-4" />}>
              Configurer les lits
            </Button>
          }
        />
      ) : (
        <div className="space-y-6">
          {Object.entries(byWard).map(([ward, wardBeds]) => (
            <Card key={ward}>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <h2 className="text-sm font-semibold text-text-primary">{ward}</h2>
                  <span className="text-xs text-text-muted font-medium">
                    {wardBeds.filter((b) => b.bedStatus === 'OCCUPIED').length} / {wardBeds.length} lit(s) occupé(s)
                  </span>
                </div>
              </CardHeader>
              <CardBody>
                <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
                  {wardBeds.map((bed) => {
                    const isUnmatched = bed.encounterId > 0 && !configuredBeds.some(cb => 
                      bed.ward?.toLowerCase().trim() === cb.ward.toLowerCase().trim() &&
                      bed.room?.toLowerCase().trim() === cb.room.toLowerCase().trim() &&
                      bed.bed?.toLowerCase().trim() === cb.bed.toLowerCase().trim()
                    )

                    return (
                      <div
                        key={`${bed.ward}-${bed.room}-${bed.bed}`}
                        className={`rounded-lg border-2 p-3 transition-all flex flex-col justify-between min-h-[110px] ${getBedColor(bed.bedStatus)}`}
                      >
                        <div className="flex items-start justify-between mb-2 gap-1">
                          <div className="min-w-0">
                            <p className="text-xs font-bold text-text-primary truncate">
                              {bed.room} / {bed.bed}
                            </p>
                          </div>
                          <Badge variant={getBedVariant(bed.bedStatus)} className="text-2xs whitespace-nowrap shrink-0">
                            {getBedLabel(bed.bedStatus)}
                          </Badge>
                        </div>

                        {bed.bedStatus === 'OCCUPIED' ? (
                          <div className="mt-1 flex-1">
                            <p className="text-xs font-semibold text-text-primary truncate">{bed.patientName || 'Patient'}</p>
                            <p className="text-2xs text-text-muted font-mono">{bed.mrn}</p>
                            {bed.admissionDate && (
                              <p className="text-2xs text-text-muted mt-1">
                                Adm. {format(new Date(bed.admissionDate), 'dd/MM')}
                              </p>
                            )}
                            {isUnmatched && (
                              <div className="flex items-center gap-1 mt-1 text-2xs text-clinical-warning font-semibold">
                                <AlertTriangle className="h-3 w-3 shrink-0" />
                                <span>Lit non enregistré</span>
                              </div>
                            )}
                          </div>
                        ) : (
                          <div className="mt-auto pt-2 border-t border-surface-border flex items-center justify-between">
                            <select
                              value={bed.bedStatus}
                              onChange={(e) => handleUpdateBedStatus(bed.ward!, bed.room!, bed.bed!, e.target.value as ConfiguredBed['status'])}
                              className="text-2xs bg-transparent border-0 text-text-muted focus:ring-0 cursor-pointer hover:text-text-primary p-0"
                            >
                              <option value="AVAILABLE">Disponible</option>
                              <option value="CLEANING">Nettoyage</option>
                              <option value="MAINTENANCE">Maintenance</option>
                            </select>
                          </div>
                        )}
                      </div>
                    )
                  })}
                </div>
              </CardBody>
            </Card>
          ))}
        </div>
      )}

      {/* Modal Configuration des lits */}
      <Modal open={showConfigModal} onClose={() => setShowConfigModal(false)} title="Configuration des lits du centre médical" size="xl">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Formulaire ajout */}
          <form onSubmit={handleAddBed} className="space-y-4 md:border-r md:pr-6 border-surface-border">
            <h3 className="text-sm font-semibold text-text-primary">Ajouter un lit</h3>
            <Input
              label="Unité / Service *"
              required
              placeholder="ex: Médecine générale"
              value={newBedForm.ward || ''}
              onChange={(e) => setNewBedForm({ ...newBedForm, ward: e.target.value })}
            />
            <Input
              label="Chambre *"
              required
              placeholder="ex: Chambre 101"
              value={newBedForm.room || ''}
              onChange={(e) => setNewBedForm({ ...newBedForm, room: e.target.value })}
            />
            <Input
              label="Lit *"
              required
              placeholder="ex: Lit A"
              value={newBedForm.bed || ''}
              onChange={(e) => setNewBedForm({ ...newBedForm, bed: e.target.value })}
            />
            <Select
              label="État initial"
              value={newBedForm.status || 'AVAILABLE'}
              onChange={(e) => setNewBedForm({ ...newBedForm, status: e.target.value as ConfiguredBed['status'] })}
              options={[
                { value: 'AVAILABLE', label: 'Disponible' },
                { value: 'CLEANING', label: 'Nettoyage' },
                { value: 'MAINTENANCE', label: 'Maintenance' },
              ]}
            />
            <Button type="submit" icon={<Plus className="h-4 w-4" />} className="w-full">
              Ajouter le lit
            </Button>
          </form>

          {/* Liste des lits */}
          <div className="md:col-span-2 space-y-4">
            <h3 className="text-sm font-semibold text-text-primary flex items-center justify-between">
              <span>Lits enregistrés ({configuredBeds.length})</span>
            </h3>
            <div className="max-h-[350px] overflow-y-auto border border-surface-border rounded-lg divide-y divide-surface-border scrollbar-thin">
              {configuredBeds.length === 0 ? (
                <div className="p-8 text-center text-text-muted text-sm"> Aucun lit configuré.</div>
              ) : (
                configuredBeds.map(b => (
                  <div key={b.id} className="p-3 flex items-center justify-between hover:bg-slate-50 transition-colors">
                    <div>
                      <p className="text-xs font-bold text-text-primary">{b.ward}</p>
                      <p className="text-sm text-text-muted font-medium">{b.room} — {b.bed}</p>
                    </div>
                    <div className="flex items-center gap-3">
                      <Badge variant={getBedVariant(b.status)} className="text-2xs">
                        {getBedLabel(b.status)}
                      </Badge>
                      <button
                        type="button"
                        onClick={() => handleDeleteBed(b.id)}
                        className="text-text-muted hover:text-clinical-danger transition-colors cursor-pointer"
                        aria-label="Supprimer le lit"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
        <div className="flex justify-end pt-4 mt-6 border-t border-surface-border">
          <Button variant="outline" onClick={() => setShowConfigModal(false)}>Fermer</Button>
        </div>
      </Modal>
    </div>
  )
}
