import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Search, Shield, Settings, CheckCircle2, AlertTriangle, XCircle, Globe, Mail, Phone } from 'lucide-react'
import {
  Button,
  Badge,
  Card,
  CardHeader,
  CardBody,
  Input,
  Select,
  Spinner,
  EmptyState,
  Table,
  Thead,
  Th,
  Tr,
  Td,
  Modal,
  StatCard,
} from '@/components/ui'
import { tenantService } from '@/services/tenant.service'
import { format } from 'date-fns'
import type { Tenant } from '@/types'

export default function TenantsPage() {
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('')
  const [showCreate, setShowCreate] = useState(false)
  const [editingTenant, setEditingTenant] = useState<Tenant | null>(null)
  const [form, setForm] = useState<Partial<Tenant>>({
    id: '',
    name: '',
    domain: '',
    contactEmail: '',
    contactPhone: '',
    status: 'ACTIVE',
  })

  // Fetch Tenants list
  const { data: tenants, isLoading } = useQuery({
    queryKey: ['tenants'],
    queryFn: tenantService.list,
    staleTime: 30_000,
  })

  // Create mutation
  const createMutation = useMutation({
    mutationFn: tenantService.create,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['tenants'] })
      setShowCreate(false)
      resetForm()
    },
  })

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<Tenant> }) =>
      tenantService.update(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['tenants'] })
      setEditingTenant(null)
      resetForm()
    },
  })

  // Status mutation
  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' }) =>
      tenantService.updateStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['tenants'] })
    },
  })

  const resetForm = () => {
    setForm({
      id: '',
      name: '',
      domain: '',
      contactEmail: '',
      contactPhone: '',
      status: 'ACTIVE',
    })
  }

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault()
    createMutation.mutate(form)
  }

  const handleUpdate = (e: React.FormEvent) => {
    e.preventDefault()
    if (!editingTenant) return
    const { id: _id, ...data } = form
    void _id
    updateMutation.mutate({ id: editingTenant.id, data })
  }

  const startEdit = (tenant: Tenant) => {
    setEditingTenant(tenant)
    setForm({
      id: tenant.id,
      name: tenant.name,
      domain: tenant.domain || '',
      contactEmail: tenant.contactEmail || '',
      contactPhone: tenant.contactPhone || '',
      status: tenant.status,
    })
  }

  const handleStatusChange = (id: string, status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED') => {
    statusMutation.mutate({ id, status })
  }

  // Filter logic
  const filteredTenants = tenants?.filter((t) => {
    const matchesSearch =
      t.name.toLowerCase().includes(search.toLowerCase()) ||
      t.id.toLowerCase().includes(search.toLowerCase()) ||
      (t.domain && t.domain.toLowerCase().includes(search.toLowerCase()))
    const matchesStatus = statusFilter === '' || t.status === statusFilter
    return matchesSearch && matchesStatus
  })

  // Stats calculation
  const totalCount = tenants?.length || 0
  const activeCount = tenants?.filter((t) => t.status === 'ACTIVE').length || 0
  const suspendedCount = tenants?.filter((t) => t.status === 'SUSPENDED').length || 0

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-text-primary">Gestion des Tenants</h1>
          <p className="text-sm text-text-muted mt-0.5">
            Administration des hôpitaux et des espaces de travail SaaS
          </p>
        </div>
        <Button icon={<Plus className="h-4 w-4" />} onClick={() => { resetForm(); setShowCreate(true) }}>
          Enregistrer un Hôpital
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
        <StatCard
          label="Total Établissements"
          value={totalCount.toString()}
          icon={<Shield className="h-5 w-5 text-primary-500" />}
        />
        <StatCard
          label="Actifs"
          value={activeCount.toString()}
          icon={<CheckCircle2 className="h-5 w-5 text-emerald-500" />}
        />
        <StatCard
          label="Suspendus"
          value={suspendedCount.toString()}
          icon={<AlertTriangle className="h-5 w-5 text-red-500" />}
        />
      </div>

      <Card>
        <CardHeader className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <Input
            placeholder="Rechercher par nom, ID ou domaine..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            icon={<Search className="h-4 w-4" />}
            className="max-w-sm"
          />
          <Select
            options={[
              { label: 'Tous les statuts', value: '' },
              { label: 'Actif', value: 'ACTIVE' },
              { label: 'Inactif', value: 'INACTIVE' },
              { label: 'Suspendu', value: 'SUSPENDED' },
            ]}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="max-w-xs"
          />
        </CardHeader>

        <CardBody className="p-0">
          {isLoading ? (
            <div className="flex justify-center py-12">
              <Spinner />
            </div>
          ) : !filteredTenants?.length ? (
            <EmptyState
              icon={<Shield className="h-10 w-10 text-slate-400" />}
              title="Aucun hôpital trouvé"
              description={search || statusFilter ? 'Ajustez vos filtres de recherche.' : 'Commencez par enregistrer un premier hôpital.'}
              action={
                !(search || statusFilter) && (
                  <Button size="sm" onClick={() => setShowCreate(true)} icon={<Plus className="h-4 w-4" />}>
                    Créer un tenant
                  </Button>
                )
              }
            />
          ) : (
            <Table>
              <Thead>
                <Tr>
                  <Th>Identifiant (ID)</Th>
                  <Th>Nom de l'Hôpital</Th>
                  <Th>Plan</Th>
                  <Th>Domaine / DNS</Th>
                  <Th>Contact</Th>
                  <Th>Statut</Th>
                  <Th>Date de création</Th>
                  <Th className="text-right">Actions</Th>
                </Tr>
              </Thead>
              <tbody>
                {filteredTenants.map((t) => (
                  <Tr key={t.id}>
                    <Td className="font-semibold text-slate-800">{t.id}</Td>
                    <Td>{t.name}</Td>
                    <Td>
                      {t.planName ? (
                        <div>
                          <span className="font-medium">{t.planName}</span>
                          {t.subscriptionStatus && (
                            <div className="text-xs text-slate-500">{t.subscriptionStatus}</div>
                          )}
                        </div>
                      ) : (
                        <span className="text-xs text-text-muted">—</span>
                      )}
                    </Td>
                    <Td>
                      {t.domain ? (
                        <span className="inline-flex items-center gap-1.5 text-text-secondary">
                          <Globe className="h-3.5 w-3.5 text-slate-400" />
                          {t.domain}
                        </span>
                      ) : (
                        <span className="text-text-muted text-xs">Non configuré</span>
                      )}
                    </Td>
                    <Td>
                      <div className="flex flex-col space-y-0.5 text-xs text-text-secondary">
                        {t.contactEmail && (
                          <span className="flex items-center gap-1">
                            <Mail className="h-3 w-3" />
                            {t.contactEmail}
                          </span>
                        )}
                        {t.contactPhone && (
                          <span className="flex items-center gap-1">
                            <Phone className="h-3 w-3" />
                            {t.contactPhone}
                          </span>
                        )}
                      </div>
                    </Td>
                    <Td>
                      <Badge
                        variant={
                          t.status === 'ACTIVE'
                            ? 'normal'
                            : t.status === 'SUSPENDED'
                            ? 'danger'
                            : 'neutral'
                        }
                      >
                        {t.status === 'ACTIVE'
                          ? 'Actif'
                          : t.status === 'SUSPENDED'
                          ? 'Suspendu'
                          : 'Inactif'}
                      </Badge>
                    </Td>
                    <Td>
                      {t.createdAt ? format(new Date(t.createdAt), 'dd/MM/yyyy HH:mm') : '—'}
                    </Td>
                    <Td className="text-right">
                      <div className="inline-flex gap-2">
                        <Button size="sm" variant="outline" icon={<Settings className="h-3.5 w-3.5" />} onClick={() => startEdit(t)}>
                          Modifier
                        </Button>
                        {t.status === 'ACTIVE' ? (
                          <Button
                            size="sm"
                            variant="danger"
                            icon={<XCircle className="h-3.5 w-3.5" />}
                            onClick={() => handleStatusChange(t.id, 'SUSPENDED')}
                          >
                            Suspendre
                          </Button>
                        ) : (
                          <Button
                            size="sm"
                            variant="secondary"
                            icon={<CheckCircle2 className="h-3.5 w-3.5" />}
                            onClick={() => handleStatusChange(t.id, 'ACTIVE')}
                          >
                            Activer
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

      {/* Creation Modal */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Enregistrer un nouveau centre">
        <form onSubmit={handleCreate} className="space-y-4">
          <Input
            label="Identifiant unique (ID)"
            placeholder="Ex: hospital-libreville"
            value={form.id}
            onChange={(e) => setForm({ ...form, id: e.target.value.toLowerCase().replace(/\s+/g, '-') })}
            required
          />
          <Input
            label="Nom de l'hôpital"
            placeholder="Ex: Hôpital d'Instruction des Armées"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
          <Input
            label="Sous-domaine DNS"
            placeholder="Ex: hiagl.ehealth.saas"
            value={form.domain}
            onChange={(e) => setForm({ ...form, domain: e.target.value })}
          />
          <Input
            label="Adresse email de contact"
            type="email"
            placeholder="contact@hia.org"
            value={form.contactEmail}
            onChange={(e) => setForm({ ...form, contactEmail: e.target.value })}
          />
          <Input
            label="Téléphone de contact"
            placeholder="+241 01 23 45 67"
            value={form.contactPhone}
            onChange={(e) => setForm({ ...form, contactPhone: e.target.value })}
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="ghost" type="button" onClick={() => setShowCreate(false)}>
              Annuler
            </Button>
            <Button type="submit" loading={createMutation.isPending}>
              Enregistrer
            </Button>
          </div>
        </form>
      </Modal>

      {/* Editing Modal */}
      <Modal open={!!editingTenant} onClose={() => setEditingTenant(null)} title="Modifier l'établissement">
        <form onSubmit={handleUpdate} className="space-y-4">
          <Input
            label="Identifiant unique (ID)"
            value={form.id}
            disabled
          />
          <Input
            label="Nom de l'hôpital"
            placeholder="Ex: Hôpital d'Instruction des Armées"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
          <Input
            label="Sous-domaine DNS"
            placeholder="Ex: hiagl.ehealth.saas"
            value={form.domain}
            onChange={(e) => setForm({ ...form, domain: e.target.value })}
          />
          <Input
            label="Adresse email de contact"
            type="email"
            placeholder="contact@hia.org"
            value={form.contactEmail}
            onChange={(e) => setForm({ ...form, contactEmail: e.target.value })}
          />
          <Input
            label="Téléphone de contact"
            placeholder="+241 01 23 45 67"
            value={form.contactPhone}
            onChange={(e) => setForm({ ...form, contactPhone: e.target.value })}
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="ghost" type="button" onClick={() => setEditingTenant(null)}>
              Annuler
            </Button>
            <Button type="submit" loading={updateMutation.isPending}>
              Sauvegarder
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
