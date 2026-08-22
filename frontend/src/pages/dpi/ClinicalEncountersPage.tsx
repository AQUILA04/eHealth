import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { FileText, Plus, Search } from 'lucide-react'
import {
  Button, Badge, Card, CardHeader, CardBody, Input,
  Spinner, EmptyState, Table, Thead, Th, Tr, Td, Modal, Select
} from '@/components/ui'
import { dpiEncounterService } from '@/services/dpi.service'
import { gapEncounterService, gapPatientService } from '@/services/gap.service'
import { format } from 'date-fns'
import type { ClinicalEncounter, ClinicalStatus } from '@/types'

const STATUS_LABELS: Record<ClinicalStatus, string> = {
  OPEN: 'Ouvert', CLOSED: 'Clôturé', SUSPENDED: 'Suspendu',
}
const STATUS_VARIANTS: Record<ClinicalStatus, 'normal' | 'neutral' | 'warning'> = {
  OPEN: 'normal', CLOSED: 'neutral', SUSPENDED: 'warning',
}

export default function ClinicalEncountersPage() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [patientRef, setPatientRef] = useState('')
  const [searchedRef, setSearchedRef] = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState<Partial<ClinicalEncounter>>({})
  const [patientEncounters, setPatientEncounters] = useState<any[]>([])
  const [loadingEncounters, setLoadingEncounters] = useState(false)

  const handlePatientRefBlur = async (ref: string) => {
    if (!ref) return
    setLoadingEncounters(true)
    try {
      const patient = await gapPatientService.getByMrn(ref)
      const encs = await gapEncounterService.getByPatient(patient.id)
      setPatientEncounters(encs)
      if (encs.length > 0) {
        setForm(prev => ({
          ...prev,
          gapEncounterId: encs[0].id,
          encounterType: encs[0].encounterType,
          attendingPhysicianId: encs[0].attendingPhysicianId || '',
          attendingPhysicianName: encs[0].attendingPhysicianName || ''
        }))
      }
    } catch (err) {
      console.error(err)
      setPatientEncounters([])
    } finally {
      setLoadingEncounters(false)
    }
  }

  const { data: encounters, isLoading } = useQuery({
    queryKey: ['clinical-encounters', searchedRef],
    queryFn: () =>
      searchedRef
        ? dpiEncounterService.getByPatient(searchedRef)
        : Promise.resolve([] as ClinicalEncounter[]),
    enabled: true,
    staleTime: 30_000,
  })

  const createMutation = useMutation({
    mutationFn: dpiEncounterService.create,
    onSuccess: (enc) => {
      qc.invalidateQueries({ queryKey: ['clinical-encounters'] })
      setShowCreate(false)
      navigate(`/dpi/encounters/${enc.id}`)
    },
  })

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setSearchedRef(patientRef)
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-text-primary">Dossiers cliniques</h1>
          <p className="text-sm text-text-muted mt-0.5">Dossier Patient Informatisé (DPI)</p>
        </div>
        <Button icon={<Plus className="h-4 w-4" />} onClick={() => setShowCreate(true)}>
          Nouveau dossier
        </Button>
      </div>

      <Card>
        <CardHeader>
          <form onSubmit={handleSearch} className="flex gap-2">
            <Input
              placeholder="Référence patient (MRN ou UUID)..."
              value={patientRef}
              onChange={(e) => setPatientRef(e.target.value)}
              icon={<Search className="h-4 w-4" />}
              className="max-w-sm"
            />
            <Button type="submit" variant="secondary" size="md">Rechercher</Button>
          </form>
        </CardHeader>
        <CardBody className="p-0">
          {!searchedRef ? (
            <EmptyState
              icon={<FileText className="h-10 w-10" />}
              title="Rechercher un dossier clinique"
              description="Saisissez le MRN ou l'UUID du patient pour afficher ses dossiers cliniques."
            />
          ) : isLoading ? (
            <div className="flex justify-center py-12"><Spinner /></div>
          ) : !encounters?.length ? (
            <EmptyState
              icon={<FileText className="h-10 w-10" />}
              title="Aucun dossier clinique"
              description={`Aucun dossier trouvé pour "${searchedRef}".`}
              action={
                <Button size="sm" onClick={() => setShowCreate(true)} icon={<Plus className="h-4 w-4" />}>
                  Créer un dossier
                </Button>
              }
            />
          ) : (
            <Table>
              <Thead>
                <tr>
                  <Th>ID</Th>
                  <Th>Patient</Th>
                  <Th>Médecin référent</Th>
                  <Th>Motif principal</Th>
                  <Th>Diagnostic</Th>
                  <Th>Statut</Th>
                  <Th>Ouvert le</Th>
                </tr>
              </Thead>
              <tbody>
                {(encounters as ClinicalEncounter[]).map((enc) => (
                  <Tr key={enc.id} onClick={() => navigate(`/dpi/encounters/${enc.id}`)}>
                    <Td>
                      <span className="font-mono text-xs font-semibold text-primary-700 bg-primary-50 px-2 py-0.5 rounded">
                        DPI-{enc.id}
                      </span>
                    </Td>
                    <Td className="font-mono text-xs">{enc.patientRef}</Td>
                    <Td className="text-sm">{enc.attendingPhysicianName || enc.attendingPhysicianId}</Td>
                    <Td className="text-sm max-w-[180px] truncate">
                      {enc.chiefComplaint || <span className="text-text-muted">—</span>}
                    </Td>
                    <Td className="text-sm max-w-[180px] truncate">
                      {enc.primaryDiagnosisLabel || <span className="text-text-muted">En attente</span>}
                    </Td>
                    <Td>
                      <Badge variant={STATUS_VARIANTS[enc.status]}>
                        {STATUS_LABELS[enc.status]}
                      </Badge>
                    </Td>
                    <Td className="text-xs text-text-muted">
                      {format(new Date(enc.createdAt), 'dd/MM/yyyy HH:mm')}
                    </Td>
                  </Tr>
                ))}
              </tbody>
            </Table>
          )}
        </CardBody>
      </Card>

      {/* Modal création */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Nouveau dossier clinique" size="md">
        <form
          onSubmit={(e) => { e.preventDefault(); createMutation.mutate(form as Parameters<typeof dpiEncounterService.create>[0]) }}
          className="space-y-4"
        >
          <Input
            label="Référence patient (MRN / UUID) *"
            required
            placeholder="ex: MRN-00001"
            value={form.patientRef || ''}
            onChange={(e) => setForm({ ...form, patientRef: e.target.value })}
            onBlur={(e) => handlePatientRefBlur(e.target.value)}
          />

          {loadingEncounters && <div className="flex justify-center py-2"><Spinner size="sm" /></div>}

          {patientEncounters.length > 0 ? (
            <Select
              label="Admission GAP *"
              required
              value={form.gapEncounterId || ''}
              onChange={(e) => {
                const selectedId = parseInt(e.target.value)
                const found = patientEncounters.find((pe) => pe.id === selectedId)
                setForm({
                  ...form,
                  gapEncounterId: selectedId,
                  encounterType: found?.encounterType || 'INPATIENT',
                  attendingPhysicianId: found?.attendingPhysicianId || '',
                  attendingPhysicianName: found?.attendingPhysicianName || '',
                })
              }}
              options={patientEncounters.map((pe) => ({
                value: pe.id.toString(),
                label: `#${pe.id} - ${
                  pe.encounterType === 'INPATIENT'
                    ? 'Hospitalisation'
                    : pe.encounterType === 'OUTPATIENT'
                    ? 'Consultation'
                    : pe.encounterType
                } (${pe.ward || ''}) du ${format(new Date(pe.admissionDate), 'dd/MM/yyyy')}`,
              }))}
            />
          ) : (
            <Input
              label="ID de l'admission GAP *"
              type="number"
              required
              placeholder="ex: 1"
              value={form.gapEncounterId || ''}
              onChange={(e) => setForm({ ...form, gapEncounterId: parseInt(e.target.value) })}
            />
          )}

          <Select
            label="Type de dossier *"
            value={form.encounterType || 'INPATIENT'}
            onChange={(e) => setForm({ ...form, encounterType: e.target.value as ClinicalEncounter['encounterType'] })}
            options={[
              { value: 'INPATIENT', label: 'Hospitalisation' },
              { value: 'OUTPATIENT', label: 'Consultation' },
              { value: 'EMERGENCY', label: 'Urgences' },
              { value: 'DAY_SURGERY', label: 'Chirurgie ambulatoire' },
            ]}
          />
          <Input
            label="ID médecin référent *"
            required
            placeholder="ex: DR-001"
            value={form.attendingPhysicianId || ''}
            onChange={(e) => setForm({ ...form, attendingPhysicianId: e.target.value })}
          />
          <Input
            label="Motif de consultation"
            placeholder="ex: Douleurs thoraciques"
            value={form.chiefComplaint || ''}
            onChange={(e) => setForm({ ...form, chiefComplaint: e.target.value })}
          />
          {createMutation.isError && (
            <p className="text-sm text-clinical-danger">Erreur lors de la création du dossier.</p>
          )}
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowCreate(false)}>Annuler</Button>
            <Button type="submit" loading={createMutation.isPending}>Créer le dossier</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
