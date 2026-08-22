import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Activity, Pill, FlaskConical, Plus, ChevronLeft,
  Thermometer, Heart, Wind, Droplets
} from 'lucide-react'
import {
  Button, Badge, Card, CardHeader, CardBody, Input, Select,
  Spinner, Table, Thead, Th, Tr, Td, Modal, cn
} from '@/components/ui'
import { dpiEncounterService, dpiVitalSignService, dpiMedicationService, dpiLabOrderService } from '@/services/dpi.service'
import { Can, PERMISSIONS } from '@/auth/permissions'
import { format } from 'date-fns'
import type { VitalSign, MedicationOrder, LabOrder, MedicationRoute, MedicationFrequency } from '@/types'

const TABS = [
  { id: 'vitals', label: 'Constantes vitales', icon: <Activity className="h-4 w-4" /> },
  { id: 'medications', label: 'Prescriptions', icon: <Pill className="h-4 w-4" /> },
  { id: 'labs', label: 'Examens de labo', icon: <FlaskConical className="h-4 w-4" /> },
]

const ROUTES: { value: MedicationRoute; label: string }[] = [
  { value: 'ORAL', label: 'Oral' }, { value: 'IV', label: 'IV' }, { value: 'IM', label: 'IM' },
  { value: 'SC', label: 'SC' }, { value: 'TOPICAL', label: 'Topique' },
  { value: 'INHALATION', label: 'Inhalation' }, { value: 'OTHER', label: 'Autre' },
]
const FREQUENCIES: { value: MedicationFrequency; label: string }[] = [
  { value: 'ONCE', label: 'Dose unique' }, { value: 'DAILY', label: '1×/jour' },
  { value: 'TWICE_DAILY', label: '2×/jour' }, { value: 'THREE_TIMES_DAILY', label: '3×/jour' },
  { value: 'EVERY_4_HOURS', label: 'Toutes les 4h' }, { value: 'EVERY_6_HOURS', label: 'Toutes les 6h' },
  { value: 'EVERY_8_HOURS', label: 'Toutes les 8h' }, { value: 'EVERY_12_HOURS', label: 'Toutes les 12h' },
  { value: 'AS_NEEDED', label: 'Si besoin' },
]

const INTERPRETATION_VARIANTS: Record<string, 'normal' | 'danger' | 'warning' | 'neutral'> = {
  NORMAL: 'normal', ABNORMAL_LOW: 'warning', ABNORMAL_HIGH: 'warning',
  CRITICAL_LOW: 'danger', CRITICAL_HIGH: 'danger', INDETERMINATE: 'neutral',
}
const INTERPRETATION_LABELS: Record<string, string> = {
  NORMAL: 'Normal', ABNORMAL_LOW: 'Bas', ABNORMAL_HIGH: 'Élevé',
  CRITICAL_LOW: 'Critique ↓', CRITICAL_HIGH: 'Critique ↑', INDETERMINATE: 'Indéterminé',
}

export default function ClinicalEncounterDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const encounterId = parseInt(id!)
  const [activeTab, setActiveTab] = useState('vitals')
  const [showVitalModal, setShowVitalModal] = useState(false)
  const [showMedModal, setShowMedModal] = useState(false)
  const [showLabModal, setShowLabModal] = useState(false)
  const [showResultModal, setShowResultModal] = useState<LabOrder | null>(null)
  const [vitalForm, setVitalForm] = useState<Partial<VitalSign>>({})
  const [medForm, setMedForm] = useState<Partial<MedicationOrder>>({ route: 'ORAL', frequency: 'DAILY' })
  const [labForm, setLabForm] = useState<Partial<LabOrder>>({ urgency: 'ROUTINE' })
  const [resultForm, setResultForm] = useState({ result: '', resultUnit: '', referenceRange: '', interpretation: 'NORMAL' })

  const { data: encounter, isLoading: encLoading } = useQuery({
    queryKey: ['clinical-encounter', encounterId],
    queryFn: () => dpiEncounterService.getById(encounterId),
  })

  const { data: vitals, isLoading: vitalsLoading } = useQuery({
    queryKey: ['vitals', encounterId],
    queryFn: () => dpiVitalSignService.list(encounterId),
    enabled: activeTab === 'vitals',
  })

  const { data: medications, isLoading: medsLoading } = useQuery({
    queryKey: ['medications', encounterId],
    queryFn: () => dpiMedicationService.list(encounterId),
    enabled: activeTab === 'medications',
  })

  const { data: labOrders, isLoading: labsLoading } = useQuery({
    queryKey: ['lab-orders', encounterId],
    queryFn: () => dpiLabOrderService.list(encounterId),
    enabled: activeTab === 'labs',
  })

  const vitalMutation = useMutation({
    mutationFn: (data: Partial<VitalSign>) => dpiVitalSignService.record(encounterId, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['vitals', encounterId] }); setShowVitalModal(false); setVitalForm({}) },
  })

  const medMutation = useMutation({
    mutationFn: (data: Partial<MedicationOrder>) => dpiMedicationService.prescribe(encounterId, data as Parameters<typeof dpiMedicationService.prescribe>[1]),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['medications', encounterId] }); setShowMedModal(false); setMedForm({ route: 'ORAL', frequency: 'DAILY' }) },
  })

  const labMutation = useMutation({
    mutationFn: (data: Partial<LabOrder>) => dpiLabOrderService.order(encounterId, data as Parameters<typeof dpiLabOrderService.order>[1]),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['lab-orders', encounterId] }); setShowLabModal(false); setLabForm({ urgency: 'ROUTINE' }) },
  })

  const resultMutation = useMutation({
    mutationFn: ({ orderId, data }: { orderId: number; data: typeof resultForm }) =>
      dpiLabOrderService.recordResult(encounterId, orderId, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['lab-orders', encounterId] }); setShowResultModal(null) },
  })

  if (encLoading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>
  if (!encounter) return <div className="text-center py-20 text-text-muted">Dossier introuvable</div>

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-start gap-3">
        <Button variant="ghost" size="sm" icon={<ChevronLeft className="h-4 w-4" />} onClick={() => navigate(-1)}>
          Retour
        </Button>
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-bold text-text-primary">DPI-{encounter.id}</h1>
            <Badge variant={encounter.status === 'OPEN' ? 'normal' : encounter.status === 'SUSPENDED' ? 'warning' : 'neutral'}>
              {encounter.status === 'OPEN' ? 'Ouvert' : encounter.status === 'SUSPENDED' ? 'Suspendu' : 'Clôturé'}
            </Badge>
          </div>
          <p className="text-sm text-text-muted mt-0.5">
            Patient : <span className="font-mono font-medium">{encounter.patientRef}</span>
            {' · '}Médecin : {encounter.attendingPhysicianName || encounter.attendingPhysicianId}
            {' · '}Ouvert le {format(new Date(encounter.createdAt), 'dd/MM/yyyy HH:mm')}
          </p>
        </div>
      </div>

      {/* Motif / Diagnostic */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Card className="p-4">
          <p className="text-xs font-semibold uppercase tracking-wider text-text-muted mb-1">Motif de consultation</p>
          <p className="text-sm text-text-primary">{encounter.chiefComplaint || <span className="text-text-muted italic">Non renseigné</span>}</p>
        </Card>
        <Card className="p-4">
          <p className="text-xs font-semibold uppercase tracking-wider text-text-muted mb-1">Diagnostic</p>
          <p className="text-sm text-text-primary">{encounter.primaryDiagnosisLabel || <span className="text-text-muted italic">En attente</span>}</p>
        </Card>
      </div>

      {/* Tabs */}
      <Card>
        <div className="border-b border-surface-border">
          <nav className="flex overflow-x-auto scrollbar-thin">
            {TABS.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={cn(
                  'flex items-center gap-2 px-5 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap cursor-pointer',
                  activeTab === tab.id
                    ? 'border-primary-600 text-primary-600'
                    : 'border-transparent text-text-muted hover:text-text-primary hover:border-surface-border-strong'
                )}
              >
                {tab.icon}
                {tab.label}
              </button>
            ))}
          </nav>
        </div>

        {/* Constantes vitales */}
        {activeTab === 'vitals' && (
          <div>
            <CardHeader>
              <div className="flex items-center justify-between">
                <h2 className="text-sm font-semibold text-text-primary">Constantes vitales</h2>
                <Can permission={PERMISSIONS.DPI_VITAL_RECORD}><Button size="sm" icon={<Plus className="h-4 w-4" />} onClick={() => setShowVitalModal(true)}>
                  Saisir
                </Button></Can>
              </div>
            </CardHeader>
            <CardBody className="p-0">
              {vitalsLoading ? (
                <div className="flex justify-center py-8"><Spinner /></div>
              ) : !vitals?.length ? (
                <div className="flex flex-col items-center py-8 text-text-muted">
                  <Activity className="h-8 w-8 mb-2" />
                  <p className="text-sm">Aucune constante enregistrée</p>
                </div>
              ) : (
                <Table>
                  <Thead>
                    <tr>
                      <Th>Date / Heure</Th>
                      <Th><span className="flex items-center gap-1"><Heart className="h-3 w-3" />TA (mmHg)</span></Th>
                      <Th><span className="flex items-center gap-1"><Heart className="h-3 w-3" />FC (bpm)</span></Th>
                      <Th><span className="flex items-center gap-1"><Thermometer className="h-3 w-3" />Temp (°C)</span></Th>
                      <Th><span className="flex items-center gap-1"><Wind className="h-3 w-3" />FR (/min)</span></Th>
                      <Th><span className="flex items-center gap-1"><Droplets className="h-3 w-3" />SpO₂ (%)</span></Th>
                      <Th>IMC</Th>
                      <Th>Par</Th>
                    </tr>
                  </Thead>
                  <tbody>
                    {(vitals as VitalSign[])
                      .sort((a, b) => new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime())
                      .map((v) => (
                        <Tr key={v.id}>
                          <Td className="text-xs font-mono">{format(new Date(v.recordedAt), 'dd/MM HH:mm')}</Td>
                          <Td>
                            {v.systolicBp && v.diastolicBp ? (
                              <span className={cn('font-medium', v.systolicBp > 140 || v.diastolicBp > 90 ? 'text-clinical-danger' : 'text-text-primary')}>
                                {v.systolicBp}/{v.diastolicBp}
                              </span>
                            ) : '—'}
                          </Td>
                          <Td>
                            {v.heartRate ? (
                              <span className={cn('font-medium', (v.heartRate < 60 || v.heartRate > 100) ? 'text-clinical-warning' : 'text-text-primary')}>
                                {v.heartRate}
                              </span>
                            ) : '—'}
                          </Td>
                          <Td>
                            {v.temperature ? (
                              <span className={cn('font-medium', v.temperature > 38.5 ? 'text-clinical-danger' : v.temperature > 37.5 ? 'text-clinical-warning' : 'text-text-primary')}>
                                {v.temperature.toFixed(1)}
                              </span>
                            ) : '—'}
                          </Td>
                          <Td>{v.respiratoryRate ?? '—'}</Td>
                          <Td>
                            {v.oxygenSaturation ? (
                              <span className={cn('font-medium', v.oxygenSaturation < 95 ? 'text-clinical-danger' : 'text-text-primary')}>
                                {v.oxygenSaturation}%
                              </span>
                            ) : '—'}
                          </Td>
                          <Td>{v.bmi ? v.bmi.toFixed(1) : '—'}</Td>
                          <Td className="text-xs text-text-muted">{v.recordedBy || '—'}</Td>
                        </Tr>
                      ))}
                  </tbody>
                </Table>
              )}
            </CardBody>
          </div>
        )}

        {/* Prescriptions */}
        {activeTab === 'medications' && (
          <div>
            <CardHeader>
              <div className="flex items-center justify-between">
                <h2 className="text-sm font-semibold text-text-primary">Prescriptions (CPOE)</h2>
                <Can permission={PERMISSIONS.DPI_MEDICATION_PRESCRIBE}><Button size="sm" icon={<Plus className="h-4 w-4" />} onClick={() => setShowMedModal(true)}>
                  Prescrire
                </Button></Can>
              </div>
            </CardHeader>
            <CardBody className="p-0">
              {medsLoading ? (
                <div className="flex justify-center py-8"><Spinner /></div>
              ) : !medications?.length ? (
                <div className="flex flex-col items-center py-8 text-text-muted">
                  <Pill className="h-8 w-8 mb-2" />
                  <p className="text-sm">Aucune prescription</p>
                </div>
              ) : (
                <Table>
                  <Thead>
                    <tr>
                      <Th>Médicament</Th>
                      <Th>Posologie</Th>
                      <Th>Voie</Th>
                      <Th>Fréquence</Th>
                      <Th>Prescrit par</Th>
                      <Th>Statut</Th>
                      <Th>Date</Th>
                    </tr>
                  </Thead>
                  <tbody>
                    {(medications as MedicationOrder[]).map((m) => (
                      <Tr key={m.id}>
                        <Td className="font-medium">{m.medicationName}</Td>
                        <Td>{m.dose} {m.unit}</Td>
                        <Td className="text-sm">{ROUTES.find((r) => r.value === m.route)?.label ?? m.route}</Td>
                        <Td className="text-sm">{FREQUENCIES.find((f) => f.value === m.frequency)?.label ?? m.frequency}</Td>
                        <Td className="text-sm">{m.prescribedBy || m.prescribedById}</Td>
                        <Td>
                          <Badge variant={
                            m.status === 'VALIDATED' ? 'normal' :
                            m.status === 'CANCELLED' ? 'danger' :
                            m.status === 'ADMINISTERED' ? 'info' : 'neutral'
                          }>
                            {m.status === 'PENDING' ? 'En attente' :
                             m.status === 'VALIDATED' ? 'Validé' :
                             m.status === 'DISPENSED' ? 'Dispensé' :
                             m.status === 'ADMINISTERED' ? 'Administré' : 'Annulé'}
                          </Badge>
                        </Td>
                        <Td className="text-xs text-text-muted">
                          {format(new Date(m.prescribedAt), 'dd/MM HH:mm')}
                        </Td>
                      </Tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </CardBody>
          </div>
        )}

        {/* Examens de labo */}
        {activeTab === 'labs' && (
          <div>
            <CardHeader>
              <div className="flex items-center justify-between">
                <h2 className="text-sm font-semibold text-text-primary">Examens de laboratoire</h2>
                <Can permission={PERMISSIONS.DPI_LAB_ORDER_CREATE}><Button size="sm" icon={<Plus className="h-4 w-4" />} onClick={() => setShowLabModal(true)}>
                  Demander
                </Button></Can>
              </div>
            </CardHeader>
            <CardBody className="p-0">
              {labsLoading ? (
                <div className="flex justify-center py-8"><Spinner /></div>
              ) : !labOrders?.length ? (
                <div className="flex flex-col items-center py-8 text-text-muted">
                  <FlaskConical className="h-8 w-8 mb-2" />
                  <p className="text-sm">Aucune demande d'examen</p>
                </div>
              ) : (
                <Table>
                  <Thead>
                    <tr>
                      <Th>Examen</Th>
                      <Th>Urgence</Th>
                      <Th>Résultat</Th>
                      <Th>Valeurs réf.</Th>
                      <Th>Interprétation</Th>
                      <Th>Statut</Th>
                      <Th>Actions</Th>
                    </tr>
                  </Thead>
                  <tbody>
                    {(labOrders as LabOrder[]).map((lab) => (
                      <Tr key={lab.id}>
                        <Td>
                          <div>
                            <p className="font-medium text-text-primary">{lab.examName}</p>
                            {lab.examCode && <p className="text-xs text-text-muted font-mono">{lab.examCode}</p>}
                          </div>
                        </Td>
                        <Td>
                          <Badge variant={lab.urgency === 'STAT' ? 'danger' : lab.urgency === 'URGENT' ? 'warning' : 'neutral'}>
                            {lab.urgency === 'STAT' ? 'STAT' : lab.urgency === 'URGENT' ? 'Urgent' : 'Routine'}
                          </Badge>
                        </Td>
                        <Td>
                          {lab.result ? (
                            <span className="font-mono text-sm font-semibold">
                              {lab.result} {lab.resultUnit}
                            </span>
                          ) : <span className="text-text-muted text-sm">—</span>}
                        </Td>
                        <Td className="text-xs text-text-muted">{lab.referenceRange || '—'}</Td>
                        <Td>
                          {lab.interpretation ? (
                            <Badge variant={INTERPRETATION_VARIANTS[lab.interpretation]}>
                              {INTERPRETATION_LABELS[lab.interpretation]}
                            </Badge>
                          ) : '—'}
                        </Td>
                        <Td>
                          <Badge variant={
                            lab.status === 'COMPLETED' ? 'normal' :
                            lab.status === 'CANCELLED' ? 'danger' :
                            lab.status === 'IN_PROGRESS' ? 'info' : 'neutral'
                          }>
                            {lab.status === 'ORDERED' ? 'Demandé' :
                             lab.status === 'COLLECTED' ? 'Prélevé' :
                             lab.status === 'IN_PROGRESS' ? 'En cours' :
                             lab.status === 'COMPLETED' ? 'Terminé' : 'Annulé'}
                          </Badge>
                        </Td>
                        <Td>
                          {lab.status !== 'COMPLETED' && lab.status !== 'CANCELLED' && (
                            <Can permission={PERMISSIONS.DPI_LAB_RESULT_ENTER}><Button
                              size="sm"
                              variant="secondary"
                              onClick={() => { setShowResultModal(lab); setResultForm({ result: '', resultUnit: '', referenceRange: '', interpretation: 'NORMAL' }) }}
                            >
                              Saisir résultat
                            </Button></Can>
                          )}
                        </Td>
                      </Tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </CardBody>
          </div>
        )}
      </Card>

      {/* Modal constantes vitales */}
      <Modal open={showVitalModal} onClose={() => setShowVitalModal(false)} title="Saisir les constantes vitales" size="lg">
        <form onSubmit={(e) => { e.preventDefault(); vitalMutation.mutate(vitalForm) }} className="space-y-4">
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
            <Input label="TA systolique (mmHg)" type="number" value={vitalForm.systolicBp || ''} onChange={(e) => setVitalForm({ ...vitalForm, systolicBp: parseInt(e.target.value) || undefined })} />
            <Input label="TA diastolique (mmHg)" type="number" value={vitalForm.diastolicBp || ''} onChange={(e) => setVitalForm({ ...vitalForm, diastolicBp: parseInt(e.target.value) || undefined })} />
            <Input label="Fréquence cardiaque (bpm)" type="number" value={vitalForm.heartRate || ''} onChange={(e) => setVitalForm({ ...vitalForm, heartRate: parseInt(e.target.value) || undefined })} />
            <Input label="Température (°C)" type="number" step="0.1" value={vitalForm.temperature || ''} onChange={(e) => setVitalForm({ ...vitalForm, temperature: parseFloat(e.target.value) || undefined })} />
            <Input label="Fréquence respiratoire (/min)" type="number" value={vitalForm.respiratoryRate || ''} onChange={(e) => setVitalForm({ ...vitalForm, respiratoryRate: parseInt(e.target.value) || undefined })} />
            <Input label="SpO₂ (%)" type="number" min={0} max={100} value={vitalForm.oxygenSaturation || ''} onChange={(e) => setVitalForm({ ...vitalForm, oxygenSaturation: parseInt(e.target.value) || undefined })} />
            <Input label="Poids (kg)" type="number" step="0.1" value={vitalForm.weightKg || ''} onChange={(e) => setVitalForm({ ...vitalForm, weightKg: parseFloat(e.target.value) || undefined })} />
            <Input label="Taille (cm)" type="number" value={vitalForm.heightCm || ''} onChange={(e) => setVitalForm({ ...vitalForm, heightCm: parseInt(e.target.value) || undefined })} />
            <Input label="Infirmier(ère)" value={vitalForm.recordedBy || ''} onChange={(e) => setVitalForm({ ...vitalForm, recordedBy: e.target.value })} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowVitalModal(false)}>Annuler</Button>
            <Button type="submit" loading={vitalMutation.isPending}>Enregistrer</Button>
          </div>
        </form>
      </Modal>

      {/* Modal prescription */}
      <Modal open={showMedModal} onClose={() => setShowMedModal(false)} title="Nouvelle prescription (CPOE)" size="lg">
        <form onSubmit={(e) => { e.preventDefault(); medMutation.mutate(medForm) }} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input label="Médicament *" required value={medForm.medicationName || ''} onChange={(e) => setMedForm({ ...medForm, medicationName: e.target.value })} />
            <Input label="Dose *" required placeholder="ex: 500" value={medForm.dose || ''} onChange={(e) => setMedForm({ ...medForm, dose: e.target.value })} />
            <Input label="Unité *" required placeholder="ex: mg" value={medForm.unit || ''} onChange={(e) => setMedForm({ ...medForm, unit: e.target.value })} />
            <Select label="Voie d'administration" value={medForm.route || 'ORAL'} onChange={(e) => setMedForm({ ...medForm, route: e.target.value as MedicationRoute })} options={ROUTES} />
            <Select label="Fréquence" value={medForm.frequency || 'DAILY'} onChange={(e) => setMedForm({ ...medForm, frequency: e.target.value as MedicationFrequency })} options={FREQUENCIES} />
            <Input label="Prescripteur (ID)" value={medForm.prescribedById || ''} onChange={(e) => setMedForm({ ...medForm, prescribedById: e.target.value })} />
            <Input label="Date de début" type="date" value={medForm.startDate || ''} onChange={(e) => setMedForm({ ...medForm, startDate: e.target.value })} />
            <Input label="Date de fin" type="date" value={medForm.endDate || ''} onChange={(e) => setMedForm({ ...medForm, endDate: e.target.value })} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowMedModal(false)}>Annuler</Button>
            <Button type="submit" loading={medMutation.isPending}>Prescrire</Button>
          </div>
        </form>
      </Modal>

      {/* Modal demande d'examen */}
      <Modal open={showLabModal} onClose={() => setShowLabModal(false)} title="Demande d'examen de laboratoire" size="md">
        <form onSubmit={(e) => { e.preventDefault(); labMutation.mutate(labForm) }} className="space-y-4">
          <Input label="Nom de l'examen *" required placeholder="ex: NFS, Glycémie, CRP..." value={labForm.examName || ''} onChange={(e) => setLabForm({ ...labForm, examName: e.target.value })} />
          <Input label="Code LOINC / local" placeholder="ex: 718-7" value={labForm.examCode || ''} onChange={(e) => setLabForm({ ...labForm, examCode: e.target.value })} />
          <Select label="Urgence" value={labForm.urgency || 'ROUTINE'} onChange={(e) => setLabForm({ ...labForm, urgency: e.target.value as LabOrder['urgency'] })} options={[{ value: 'ROUTINE', label: 'Routine' }, { value: 'URGENT', label: 'Urgent' }, { value: 'STAT', label: 'STAT (immédiat)' }]} />
          <Select label="Type d'examen *" value={labForm.orderType || 'BIOLOGY'} onChange={(e) => setLabForm({ ...labForm, orderType: e.target.value })} options={[{ value: 'BIOLOGY', label: 'Biologie' }, { value: 'MICROBIOLOGY', label: 'Microbiologie' }, { value: 'PATHOLOGY', label: 'Anatomopathologie' }, { value: 'GENETICS', label: 'Génétique' }]} />
          <Input label="Demandé par (ID)" value={labForm.orderedBy || ''} onChange={(e) => setLabForm({ ...labForm, orderedBy: e.target.value })} />
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowLabModal(false)}>Annuler</Button>
            <Button type="submit" loading={labMutation.isPending}>Envoyer la demande</Button>
          </div>
        </form>
      </Modal>

      {/* Modal saisie résultat */}
      <Modal open={!!showResultModal} onClose={() => setShowResultModal(null)} title={`Résultat — ${showResultModal?.examName}`} size="md">
        <form onSubmit={(e) => { e.preventDefault(); if (showResultModal) resultMutation.mutate({ orderId: showResultModal.id, data: resultForm }) }} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input label="Résultat *" required value={resultForm.result} onChange={(e) => setResultForm({ ...resultForm, result: e.target.value })} />
            <Input label="Unité" placeholder="ex: g/dL, mmol/L" value={resultForm.resultUnit} onChange={(e) => setResultForm({ ...resultForm, resultUnit: e.target.value })} />
            <Input label="Valeurs de référence" placeholder="ex: 4.0 - 10.0" value={resultForm.referenceRange} onChange={(e) => setResultForm({ ...resultForm, referenceRange: e.target.value })} />
            <Select label="Interprétation" value={resultForm.interpretation} onChange={(e) => setResultForm({ ...resultForm, interpretation: e.target.value })} options={Object.entries(INTERPRETATION_LABELS).map(([v, l]) => ({ value: v, label: l }))} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowResultModal(null)}>Annuler</Button>
            <Button type="submit" loading={resultMutation.isPending}>Valider le résultat</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
