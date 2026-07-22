import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { FlaskConical, Search } from 'lucide-react'
import { Card, CardHeader, CardBody, Input, Spinner, EmptyState, Table, Thead, Th, Tr, Td, Badge, Button } from '@/components/ui'
import { dpiLabOrderService } from '@/services/dpi.service'
import { format } from 'date-fns'
import type { LabOrder } from '@/types'

const STATUS_LABELS: Record<string, string> = {
  ORDERED: 'Demandé', COLLECTED: 'Prélevé', IN_PROGRESS: 'En cours',
  COMPLETED: 'Terminé', CANCELLED: 'Annulé',
}
const STATUS_VARIANTS: Record<string, 'normal' | 'info' | 'danger' | 'neutral' | 'warning'> = {
  ORDERED: 'neutral', COLLECTED: 'warning', IN_PROGRESS: 'info',
  COMPLETED: 'normal', CANCELLED: 'danger',
}
const INTERP_LABELS: Record<string, string> = {
  NORMAL: 'Normal', ABNORMAL_LOW: 'Bas', ABNORMAL_HIGH: 'Élevé',
  CRITICAL_LOW: 'Critique ↓', CRITICAL_HIGH: 'Critique ↑', INDETERMINATE: 'Indéterminé',
}
const INTERP_VARIANTS: Record<string, 'normal' | 'danger' | 'warning' | 'neutral'> = {
  NORMAL: 'normal', ABNORMAL_LOW: 'warning', ABNORMAL_HIGH: 'warning',
  CRITICAL_LOW: 'danger', CRITICAL_HIGH: 'danger', INDETERMINATE: 'neutral',
}

export default function LabOrdersPage() {
  const [encounterId, setEncounterId] = useState('')
  const [searchedId, setSearchedId] = useState<number | null>(null)

  const { data: labOrders, isLoading } = useQuery({
    queryKey: ['lab-orders-global', searchedId],
    queryFn: () => dpiLabOrderService.list(searchedId!),
    enabled: searchedId !== null,
  })

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-text-primary">Examens de laboratoire</h1>
        <p className="text-sm text-text-muted mt-0.5">Demandes et résultats d'examens biologiques</p>
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
            <EmptyState icon={<FlaskConical className="h-10 w-10" />} title="Saisir un ID de dossier" description="Entrez l'identifiant du dossier clinique pour afficher les examens." />
          ) : isLoading ? (
            <div className="flex justify-center py-12"><Spinner /></div>
          ) : !labOrders?.length ? (
            <EmptyState icon={<FlaskConical className="h-10 w-10" />} title="Aucun examen demandé" description={`Aucun examen pour le dossier DPI-${searchedId}.`} />
          ) : (
            <Table>
              <Thead>
                <tr>
                  <Th>Examen</Th>
                  <Th>Code</Th>
                  <Th>Urgence</Th>
                  <Th>Résultat</Th>
                  <Th>Unité</Th>
                  <Th>Valeurs réf.</Th>
                  <Th>Interprétation</Th>
                  <Th>Statut</Th>
                  <Th>Demandé le</Th>
                </tr>
              </Thead>
              <tbody>
                {(labOrders as LabOrder[]).map((lab) => (
                  <Tr key={lab.id}>
                    <Td className="font-medium">{lab.examName}</Td>
                    <Td className="font-mono text-xs">{lab.examCode || '—'}</Td>
                    <Td>
                      <Badge variant={lab.urgency === 'STAT' ? 'danger' : lab.urgency === 'URGENT' ? 'warning' : 'neutral'}>
                        {lab.urgency === 'STAT' ? 'STAT' : lab.urgency === 'URGENT' ? 'Urgent' : 'Routine'}
                      </Badge>
                    </Td>
                    <Td className="font-mono text-sm font-semibold">{lab.result || '—'}</Td>
                    <Td className="text-xs">{lab.resultUnit || '—'}</Td>
                    <Td className="text-xs text-text-muted">{lab.referenceRange || '—'}</Td>
                    <Td>
                      {lab.interpretation ? (
                        <Badge variant={INTERP_VARIANTS[lab.interpretation]}>
                          {INTERP_LABELS[lab.interpretation]}
                        </Badge>
                      ) : '—'}
                    </Td>
                    <Td><Badge variant={STATUS_VARIANTS[lab.status]}>{STATUS_LABELS[lab.status]}</Badge></Td>
                    <Td className="text-xs text-text-muted">{format(new Date(lab.orderedAt), 'dd/MM HH:mm')}</Td>
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
