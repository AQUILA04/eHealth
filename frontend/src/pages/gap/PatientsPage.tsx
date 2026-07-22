import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { UserPlus, Search, Users } from 'lucide-react'
import {
  Button, Badge, Card, CardHeader, CardBody,
  Input, Spinner, EmptyState, Table, Thead, Th, Tr, Td, Modal, Select
} from '@/components/ui'
import { gapPatientService } from '@/services/gap.service'
import { format } from 'date-fns'
import type { Patient } from '@/types'

function getGenderLabel(g: string) {
  return { MALE: 'Homme', FEMALE: 'Femme', OTHER: 'Autre', UNKNOWN: 'Inconnu' }[g] ?? g
}

function getAgeFromDob(dob: string): number {
  const birth = new Date(dob)
  const today = new Date()
  let age = today.getFullYear() - birth.getFullYear()
  const m = today.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--
  return age
}

const BLOOD_GROUPS = ['', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-']

export default function PatientsPage() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState<Partial<Patient>>({
    gender: 'UNKNOWN',
    active: true,
  })

  const { data: patients, isLoading } = useQuery({
    queryKey: ['patients', search],
    queryFn: () =>
      search.length >= 2
        ? gapPatientService.search(search)
        : gapPatientService.list(),
    staleTime: 30_000,
  })

  const createMutation = useMutation({
    mutationFn: gapPatientService.create,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['patients'] })
      setShowCreate(false)
      setForm({ gender: 'UNKNOWN', active: true })
    },
  })

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault()
    createMutation.mutate(form)
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-text-primary">Patients</h1>
          <p className="text-sm text-text-muted mt-0.5">
            {patients ? `${patients.length} patient(s) enregistré(s)` : 'Gestion des dossiers administratifs'}
          </p>
        </div>
        <Button icon={<UserPlus className="h-4 w-4" />} onClick={() => setShowCreate(true)}>
          Nouveau patient
        </Button>
      </div>

      <Card>
        <CardHeader>
          <Input
            placeholder="Rechercher par nom, prénom, MRN..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            icon={<Search className="h-4 w-4" />}
            className="max-w-sm"
          />
        </CardHeader>
        <CardBody className="p-0">
          {isLoading ? (
            <div className="flex justify-center py-12"><Spinner /></div>
          ) : !patients?.length ? (
            <EmptyState
              icon={<Users className="h-10 w-10" />}
              title="Aucun patient trouvé"
              description={search ? `Aucun résultat pour "${search}"` : 'Commencez par enregistrer un patient.'}
              action={
                <Button size="sm" onClick={() => setShowCreate(true)} icon={<UserPlus className="h-4 w-4" />}>
                  Nouveau patient
                </Button>
              }
            />
          ) : (
            <Table>
              <Thead>
                <tr>
                  <Th>MRN</Th>
                  <Th>Nom complet</Th>
                  <Th>Âge / Genre</Th>
                  <Th>Groupe sanguin</Th>
                  <Th>Contact</Th>
                  <Th>Statut</Th>
                  <Th>Enregistré le</Th>
                </tr>
              </Thead>
              <tbody>
                {(patients as Patient[]).map((p) => (
                  <Tr key={p.id} onClick={() => navigate(`/gap/patients/${p.id}`)}>
                    <Td>
                      <span className="font-mono text-xs font-semibold text-primary-700 bg-primary-50 px-2 py-0.5 rounded">
                        {p.localMrn}
                      </span>
                    </Td>
                    <Td>
                      <div>
                        <p className="font-medium text-text-primary">{p.lastName} {p.firstName}</p>
                        {p.email && <p className="text-xs text-text-muted">{p.email}</p>}
                      </div>
                    </Td>
                    <Td>
                      <div className="text-sm">
                        <span className="font-medium">{getAgeFromDob(p.dateOfBirth)} ans</span>
                        <span className="text-text-muted ml-1">· {getGenderLabel(p.gender)}</span>
                      </div>
                    </Td>
                    <Td>
                      {p.bloodGroup ? (
                        <span className="font-mono text-xs font-bold text-clinical-danger">{p.bloodGroup}</span>
                      ) : (
                        <span className="text-text-muted text-xs">—</span>
                      )}
                    </Td>
                    <Td className="text-sm">{p.phoneNumber || <span className="text-text-muted">—</span>}</Td>
                    <Td>
                      <Badge variant={p.active ? 'normal' : 'neutral'}>
                        {p.active ? 'Actif' : 'Inactif'}
                      </Badge>
                    </Td>
                    <Td className="text-xs text-text-muted">
                      {format(new Date(p.createdAt), 'dd/MM/yyyy')}
                    </Td>
                  </Tr>
                ))}
              </tbody>
            </Table>
          )}
        </CardBody>
      </Card>

      {/* Modal création patient */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Nouveau patient" size="lg">
        <form onSubmit={handleCreate} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Prénom *"
              required
              value={form.firstName || ''}
              onChange={(e) => setForm({ ...form, firstName: e.target.value })}
            />
            <Input
              label="Nom *"
              required
              value={form.lastName || ''}
              onChange={(e) => setForm({ ...form, lastName: e.target.value })}
            />
            <Input
              label="Date de naissance *"
              type="date"
              required
              value={form.dateOfBirth || ''}
              onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })}
            />
            <Select
              label="Genre"
              value={form.gender || 'UNKNOWN'}
              onChange={(e) => setForm({ ...form, gender: e.target.value as Patient['gender'] })}
              options={[
                { value: 'MALE', label: 'Homme' },
                { value: 'FEMALE', label: 'Femme' },
                { value: 'OTHER', label: 'Autre' },
                { value: 'UNKNOWN', label: 'Inconnu' },
              ]}
            />
            <Select
              label="Groupe sanguin"
              value={form.bloodGroup || ''}
              onChange={(e) => setForm({ ...form, bloodGroup: e.target.value || undefined })}
              options={BLOOD_GROUPS.map((g) => ({ value: g, label: g || '— Non renseigné' }))}
            />
            <Input
              label="Nationalité"
              value={form.nationality || ''}
              onChange={(e) => setForm({ ...form, nationality: e.target.value })}
            />
            <Input
              label="Téléphone"
              type="tel"
              value={form.phoneNumber || ''}
              onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
            />
            <Input
              label="Email"
              type="email"
              value={form.email || ''}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </div>
          <Input
            label="Adresse"
            value={form.address || ''}
            onChange={(e) => setForm({ ...form, address: e.target.value })}
          />
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Contact d'urgence — Nom"
              value={form.emergencyContactName || ''}
              onChange={(e) => setForm({ ...form, emergencyContactName: e.target.value })}
            />
            <Input
              label="Contact d'urgence — Téléphone"
              type="tel"
              value={form.emergencyContactPhone || ''}
              onChange={(e) => setForm({ ...form, emergencyContactPhone: e.target.value })}
            />
          </div>
          {createMutation.isError && (
            <p className="text-sm text-clinical-danger">
              Erreur lors de la création. Veuillez vérifier les données saisies.
            </p>
          )}
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="outline" type="button" onClick={() => setShowCreate(false)}>
              Annuler
            </Button>
            <Button type="submit" loading={createMutation.isPending}>
              Enregistrer le patient
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
