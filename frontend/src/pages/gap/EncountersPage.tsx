import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { BedDouble, Plus, ArrowRightLeft, LogOut } from 'lucide-react'
import {
  Button, Badge, Card, CardHeader, CardBody,
  Spinner, EmptyState, Table, Thead, Th, Tr, Td, Modal, Input, Select
} from '@/components/ui'
import { gapEncounterService, gapPatientService } from '@/services/gap.service'
import { format } from 'date-fns'
import type { Encounter, EncounterType } from '@/types'

const TYPE_LABELS: Record<string, string> = {
  INPATIENT: 'Hospitalisation', OUTPATIENT: 'Consultation', EMERGENCY: 'Urgences', DAY_SURGERY: 'Chirurgie ambulatoire',
}

export default function EncountersPage() {
  const qc = useQueryClient()
  const [showAdmit, setShowAdmit] = useState(false)
  const [showTransfer, setShowTransfer] = useState<Encounter | null>(null)
  const [showDischarge, setShowDischarge] = useState<Encounter | null>(null)
  const [admitForm, setAdmitForm] = useState<Partial<Encounter & { patientMrn: string }>>({
    encounterType: 'INPATIENT',
  })
  const [transferForm, setTransferForm] = useState({ ward: '', room: '', bedNumber: '', reason: '' })
  const [dischargeForm, setDischargeForm] = useState({ dischargeDisposition: 'HOME', dischargeSummary: '' })

  const { isLoading } = useQuery({
    queryKey: ['encounters'],
    queryFn: () => gapEncounterService.getByWard('ALL').catch(() => gapEncounterService.getBedBoard().then(() => [] as Encounter[])),
    staleTime: 30_000,
  })

  // Fallback: récupérer les encounters via bed-board si pas d'endpoint global
  const { data: bedBoard } = useQuery({
    queryKey: ['bed-board'],
    queryFn: gapEncounterService.getBedBoard,
    staleTime: 30_000,
  })

  const admitMutation = useMutation({
    mutationFn: async (data: Partial<Encounter & { patientMrn: string }>) => {
      const patient = await gapPatientService.getByMrn(data.patientMrn!)
      return gapEncounterService.admit({ ...data, patientId: patient.id, encounterType: data.encounterType ?? 'INPATIENT' })
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['encounters'] })
      qc.invalidateQueries({ queryKey: ['bed-board'] })
      setShowAdmit(false)
      setAdmitForm({ encounterType: 'INPATIENT' })
    },
  })

  const transferMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: typeof transferForm }) =>
      gapEncounterService.transfer(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['encounters'] })
      qc.invalidateQueries({ queryKey: ['bed-board'] })
      setShowTransfer(null)
    },
  })

  const dischargeMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: typeof dischargeForm }) =>
      gapEncounterService.discharge(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['encounters'] })
      qc.invalidateQueries({ queryKey: ['bed-board'] })
      setShowDischarge(null)
    },
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-text-primary">Admissions (ADT)</h1>
          <p className="text-sm text-text-muted mt-0.5">
            Gestion des admissions, transferts et sorties
          </p>
        </div>
        <Button icon={<Plus className="h-4 w-4" />} onClick={() => setShowAdmit(true)}>
          Nouvelle admission
        </Button>
      </div>

      {/* Bed board résumé */}
      {bedBoard && bedBoard.length > 0 && (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          {['OCCUPIED', 'AVAILABLE', 'CLEANING', 'MAINTENANCE'].map((status) => {
            const count = bedBoard.filter((b) => b.bedStatus === status).length
            const labels: Record<string, string> = {
              OCCUPIED: 'Occupés', AVAILABLE: 'Disponibles', CLEANING: 'Nettoyage', MAINTENANCE: 'Maintenance',
            }
            const colors: Record<string, string> = {
              OCCUPIED: 'border-l-clinical-danger', AVAILABLE: 'border-l-clinical-success',
              CLEANING: 'border-l-clinical-warning', MAINTENANCE: 'border-l-text-muted',
            }
            return (
              <Card key={status} className={`p-4 border-l-4 ${colors[status]}`}>
                <p className="text-2xl font-bold text-text-primary">{count}</p>
                <p className="text-xs text-text-muted mt-0.5">{labels[status]}</p>
              </Card>
            )
          })}
        </div>
      )}

      {/* Tableau des admissions actives depuis bed-board */}
      <Card>
        <CardHeader>
          <h2 className="text-sm font-semibold text-text-primary flex items-center gap-2">
            <BedDouble className="h-4 w-4 text-primary-600" />
            Patients hospitalisés
          </h2>
        </CardHeader>
        <CardBody className="p-0">
          {isLoading ? (
            <div className="flex justify-center py-12"><Spinner /></div>
          ) : !bedBoard?.length ? (
            <EmptyState
              icon={<BedDouble className="h-10 w-10" />}
              title="Aucune admission active"
              description="Enregistrez une nouvelle admission pour commencer."
              action={
                <Button size="sm" onClick={() => setShowAdmit(true)} icon={<Plus className="h-4 w-4" />}>
                  Nouvelle admission
                </Button>
              }
            />
          ) : (
            <Table>
              <Thead>
                <tr>
                  <Th>Patient</Th>
                  <Th>Unité</Th>
                  <Th>Chambre / Lit</Th>
                  <Th>Statut lit</Th>
                  <Th>Admission</Th>
                  <Th>Diagnostic</Th>
                  <Th>Actions</Th>
                </tr>
              </Thead>
              <tbody>
                {bedBoard.map((entry) => (
                  <Tr key={entry.encounterId}>
                    <Td>
                      <div>
                        <p className="font-medium text-text-primary">{entry.patientName}</p>
                        <p className="text-xs text-text-muted font-mono">{entry.mrn}</p>
                      </div>
                    </Td>
                    <Td className="font-medium">{entry.ward}</Td>
                    <Td>
                      <span className="font-mono text-xs">{entry.room} / {entry.bed}</span>
                    </Td>
                    <Td>
                      <Badge variant={entry.bedStatus === 'OCCUPIED' ? 'danger' : entry.bedStatus === 'AVAILABLE' ? 'normal' : 'warning'}>
                        {entry.bedStatus === 'OCCUPIED' ? 'Occupé' : entry.bedStatus === 'AVAILABLE' ? 'Disponible' : 'Nettoyage'}
                      </Badge>
                    </Td>
                    <Td className="text-xs text-text-muted">
                      {format(new Date(entry.admissionDate), 'dd/MM/yyyy HH:mm')}
                    </Td>
                    <Td className="text-xs max-w-[200px] truncate">
                      {entry.admittingDiagnosis || <span className="text-text-muted">—</span>}
                    </Td>
                    <Td>
                      <div className="flex gap-1">
                        <Button
                          size="sm"
                          variant="ghost"
                          icon={<ArrowRightLeft className="h-3.5 w-3.5" />}
                          onClick={() => {
                            setShowTransfer({ id: entry.encounterId } as Encounter)
                            setTransferForm({ ward: '', room: '', bedNumber: '', reason: '' })
                          }}
                        >
                          Transfert
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          icon={<LogOut className="h-3.5 w-3.5" />}
                          onClick={() => {
                             setShowDischarge({ id: entry.encounterId } as Encounter)
                             setDischargeForm({ dischargeDisposition: 'HOME', dischargeSummary: '' })
                          }}
                        >
                          Sortie
                        </Button>
                      </div>
                    </Td>
                  </Tr>
                ))}
              </tbody>
            </Table>
          )}
        </CardBody>
      </Card>

      {/* Modal admission */}
      <Modal open={showAdmit} onClose={() => setShowAdmit(false)} title="Nouvelle admission" size="lg">
        <form
          onSubmit={(e) => { e.preventDefault(); admitMutation.mutate(admitForm) }}
          className="space-y-4"
        >
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="MRN du patient *"
              required
              placeholder="ex: MRN-00001"
              value={admitForm.patientMrn || ''}
              onChange={(e) => setAdmitForm({ ...admitForm, patientMrn: e.target.value })}
            />
            <Select
              label="Type d'admission"
              value={admitForm.encounterType || 'INPATIENT'}
              onChange={(e) => setAdmitForm({ ...admitForm, encounterType: e.target.value as EncounterType })}
              options={Object.entries(TYPE_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            />
            <Input
              label="Unité / Service *"
              required
              placeholder="ex: Cardiologie"
              value={admitForm.ward || ''}
              onChange={(e) => setAdmitForm({ ...admitForm, ward: e.target.value })}
            />
            <Input
              label="Chambre"
              placeholder="ex: 201"
              value={admitForm.room || ''}
              onChange={(e) => setAdmitForm({ ...admitForm, room: e.target.value })}
            />
            <Input
              label="Lit"
              placeholder="ex: A"
              value={admitForm.bedNumber || ''}
              onChange={(e) => setAdmitForm({ ...admitForm, bedNumber: e.target.value })}
            />
            <Input
              label="Médecin admettant (ID)"
              value={admitForm.attendingPhysicianId || ''}
              onChange={(e) => setAdmitForm({ ...admitForm, attendingPhysicianId: e.target.value })}
            />
          </div>
          <Input
            label="Diagnostic d'admission"
            value={admitForm.admissionReason || ''}
            onChange={(e) => setAdmitForm({ ...admitForm, admissionReason: e.target.value })}
          />
          {admitMutation.isError && (
            <p className="text-sm text-clinical-danger">
              Erreur : MRN introuvable ou données invalides.
            </p>
          )}
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowAdmit(false)}>Annuler</Button>
            <Button type="submit" loading={admitMutation.isPending}>Admettre le patient</Button>
          </div>
        </form>
      </Modal>

      {/* Modal transfert */}
      <Modal open={!!showTransfer} onClose={() => setShowTransfer(null)} title="Transfert de patient" size="md">
        <form
          onSubmit={(e) => {
            e.preventDefault()
            if (showTransfer) transferMutation.mutate({ id: showTransfer.id, data: transferForm })
          }}
          className="space-y-4"
        >
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Nouvelle unité *"
              required
              value={transferForm.ward}
              onChange={(e) => setTransferForm({ ...transferForm, ward: e.target.value })}
            />
            <Input
              label="Chambre"
              value={transferForm.room}
              onChange={(e) => setTransferForm({ ...transferForm, room: e.target.value })}
            />
            <Input
              label="Lit"
              value={transferForm.bedNumber}
              onChange={(e) => setTransferForm({ ...transferForm, bedNumber: e.target.value })}
            />
            <Input
              label="Motif du transfert"
              value={transferForm.reason}
              onChange={(e) => setTransferForm({ ...transferForm, reason: e.target.value })}
            />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowTransfer(null)}>Annuler</Button>
            <Button type="submit" loading={transferMutation.isPending}>Confirmer le transfert</Button>
          </div>
        </form>
      </Modal>

      {/* Modal sortie */}
      <Modal open={!!showDischarge} onClose={() => setShowDischarge(null)} title="Sortie du patient" size="md">
        <form
          onSubmit={(e) => {
            e.preventDefault()
            if (showDischarge) dischargeMutation.mutate({ id: showDischarge.id, data: dischargeForm })
          }}
          className="space-y-4"
        >
          <Select
            label="Disposition de sortie *"
            required
            value={dischargeForm.dischargeDisposition}
            onChange={(e) => setDischargeForm({ ...dischargeForm, dischargeDisposition: e.target.value })}
            options={[
              { value: 'HOME', label: 'Retour à domicile' },
              { value: 'TRANSFER_INTERNAL', label: 'Transfert interne' },
              { value: 'TRANSFER_EXTERNAL', label: 'Transfert externe' },
              { value: 'DECEASED', label: 'Décédé' },
              { value: 'LEFT_AMA', label: 'Sortie contre avis médical' },
              { value: 'LONG_TERM_CARE', label: 'Soins de longue durée' },
            ]}
          />
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-text-primary">Résumé de sortie</label>
            <textarea
              className="w-full rounded-lg border border-surface-border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
              rows={3}
              value={dischargeForm.dischargeSummary}
              onChange={(e) => setDischargeForm({ ...dischargeForm, dischargeSummary: e.target.value })}
            />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowDischarge(null)}>Annuler</Button>
            <Button type="submit" variant="danger" loading={dischargeMutation.isPending}>
              Confirmer la sortie
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
