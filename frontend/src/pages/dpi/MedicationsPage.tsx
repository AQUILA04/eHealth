import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Pill, Search } from 'lucide-react'
import { Card, CardHeader, CardBody, Input, Spinner, EmptyState, Table, Thead, Th, Tr, Td, Badge, Button } from '@/components/ui'
import { dpiMedicationService } from '@/services/dpi.service'
import { format } from 'date-fns'
import type { MedicationOrder } from '@/types'

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'En attente', VALIDATED: 'Validé', DISPENSED: 'Dispensé',
  ADMINISTERED: 'Administré', CANCELLED: 'Annulé',
}
const STATUS_VARIANTS: Record<string, 'normal' | 'info' | 'danger' | 'neutral' | 'warning'> = {
  PENDING: 'neutral', VALIDATED: 'normal', DISPENSED: 'info',
  ADMINISTERED: 'info', CANCELLED: 'danger',
}
const ROUTE_LABELS: Record<string, string> = {
  ORAL: 'Oral', IV: 'IV', IM: 'IM', SC: 'SC', TOPICAL: 'Topique', INHALATION: 'Inhalation', OTHER: 'Autre',
}
const FREQ_LABELS: Record<string, string> = {
  ONCE: 'Dose unique', DAILY: '1×/j', TWICE_DAILY: '2×/j', THREE_TIMES_DAILY: '3×/j',
  EVERY_4_HOURS: '/4h', EVERY_6_HOURS: '/6h', EVERY_8_HOURS: '/8h', EVERY_12_HOURS: '/12h', AS_NEEDED: 'Si besoin',
}

export default function MedicationsPage() {
  const [encounterId, setEncounterId] = useState('')
  const [searchedId, setSearchedId] = useState<number | null>(null)

  const { data: medications, isLoading } = useQuery({
    queryKey: ['medications-global', searchedId],
    queryFn: () => dpiMedicationService.list(searchedId!),
    enabled: searchedId !== null,
  })

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-text-primary">Prescriptions (CPOE)</h1>
        <p className="text-sm text-text-muted mt-0.5">Ordonnances électroniques par dossier clinique</p>
      </div>
      <Card>
        <CardHeader>
          <form onSubmit={(e) => { e.preventDefault(); setSearchedId(parseInt(encounterId)) }} className="flex gap-2">
            <Input placeholder="ID du dossier clinique..." value={encounterId} onChange={(e) => setEncounterId(e.target.value)} icon={<Search className="h-4 w-4" />} className="max-w-xs" type="number" />
            <Button type="submit" variant="secondary">Afficher</Button>
          </form>
        </CardHeader>
        <CardBody className="p-0">
          {searchedId === null ? (
            <EmptyState icon={<Pill className="h-10 w-10" />} title="Saisir un ID de dossier" description="Entrez l'identifiant du dossier clinique pour afficher les prescriptions." />
          ) : isLoading ? (
            <div className="flex justify-center py-12"><Spinner /></div>
          ) : !medications?.length ? (
            <EmptyState icon={<Pill className="h-10 w-10" />} title="Aucune prescription" description={`Aucune prescription pour le dossier DPI-${searchedId}.`} />
          ) : (
            <Table>
              <Thead>
                <tr>
                  <Th>Médicament</Th>
                  <Th>Posologie</Th>
                  <Th>Voie</Th>
                  <Th>Fréquence</Th>
                  <Th>Début</Th>
                  <Th>Fin</Th>
                  <Th>Prescripteur</Th>
                  <Th>Statut</Th>
                  <Th>Prescrit le</Th>
                </tr>
              </Thead>
              <tbody>
                {(medications as MedicationOrder[]).map((m) => (
                  <Tr key={m.id}>
                    <Td className="font-medium">{m.medicationName}</Td>
                    <Td>{m.dose} {m.unit}</Td>
                    <Td className="text-sm">{ROUTE_LABELS[m.route] ?? m.route}</Td>
                    <Td className="text-sm">{FREQ_LABELS[m.frequency] ?? m.frequency}</Td>
                    <Td className="text-xs">{m.startDate ? format(new Date(m.startDate), 'dd/MM/yyyy') : '—'}</Td>
                    <Td className="text-xs">{m.endDate ? format(new Date(m.endDate), 'dd/MM/yyyy') : '—'}</Td>
                    <Td className="text-sm">{m.prescribedBy || m.prescribedById}</Td>
                    <Td><Badge variant={STATUS_VARIANTS[m.status]}>{STATUS_LABELS[m.status]}</Badge></Td>
                    <Td className="text-xs text-text-muted">{format(new Date(m.prescribedAt), 'dd/MM HH:mm')}</Td>
                  </Tr>
                ))}
              </tbody>
            </Table>
          )}
        </CardBody>
      </Card>
    </div>
  )
}
