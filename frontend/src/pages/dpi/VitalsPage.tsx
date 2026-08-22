import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Activity, Search } from 'lucide-react'
import {
  Card, CardHeader, CardBody, Input, Spinner, EmptyState,
  Table, Thead, Th, Tr, Td, Button, cn
} from '@/components/ui'
import { dpiVitalSignService } from '@/services/dpi.service'
import { format } from 'date-fns'
import type { VitalSign } from '@/types'

export default function VitalsPage() {
  const [encounterId, setEncounterId] = useState('')
  const [searchedId, setSearchedId] = useState<number | null>(null)

  const { data: vitals, isLoading } = useQuery({
    queryKey: ['vitals-global', searchedId],
    queryFn: () => dpiVitalSignService.list(searchedId!),
    enabled: searchedId !== null,
  })

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-text-primary">Constantes vitales</h1>
        <p className="text-sm text-text-muted mt-0.5">Historique des constantes par dossier clinique</p>
      </div>

      <Card>
        <CardHeader>
          <form
            onSubmit={(e) => { e.preventDefault(); setSearchedId(parseInt(encounterId)) }}
            className="flex gap-2"
          >
            <Input
              placeholder="ID du dossier clinique (DPI-X)..."
              value={encounterId}
              onChange={(e) => setEncounterId(e.target.value)}
              icon={<Search className="h-4 w-4" />}
              className="max-w-xs"
              type="number"
            />
            <Button type="submit" variant="secondary">Afficher</Button>
          </form>
        </CardHeader>
        <CardBody className="p-0">
          {searchedId === null ? (
            <EmptyState
              icon={<Activity className="h-10 w-10" />}
              title="Saisir un ID de dossier"
              description="Entrez l'identifiant du dossier clinique pour afficher les constantes vitales."
            />
          ) : isLoading ? (
            <div className="flex justify-center py-12"><Spinner /></div>
          ) : !vitals?.length ? (
            <EmptyState
              icon={<Activity className="h-10 w-10" />}
              title="Aucune constante enregistrée"
              description={`Aucune constante pour le dossier DPI-${searchedId}.`}
            />
          ) : (
            <Table>
              <Thead>
                <tr>
                  <Th>Date / Heure</Th>
                  <Th>TA (mmHg)</Th>
                  <Th>FC (bpm)</Th>
                  <Th>Temp (°C)</Th>
                  <Th>FR (/min)</Th>
                  <Th>SpO₂ (%)</Th>
                  <Th>Poids (kg)</Th>
                  <Th>IMC</Th>
                  <Th>Alertes</Th>
                  <Th>Par</Th>
                </tr>
              </Thead>
              <tbody>
                {(vitals as VitalSign[])
                  .sort((a, b) => new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime())
                  .map((v) => (
                    <Tr key={v.id}>
                      <Td className="font-mono text-xs">{format(new Date(v.recordedAt), 'dd/MM/yyyy HH:mm')}</Td>
                      <Td>
                        {v.systolicBp && v.diastolicBp ? (
                          <span className={cn('font-medium', (v.systolicBp > 140 || v.diastolicBp > 90) ? 'text-clinical-danger' : '')}>
                            {v.systolicBp}/{v.diastolicBp}
                          </span>
                        ) : '—'}
                      </Td>
                      <Td className={cn('font-medium', v.heartRate && (v.heartRate < 60 || v.heartRate > 100) ? 'text-clinical-warning' : '')}>
                        {v.heartRate ?? '—'}
                      </Td>
                      <Td className={cn('font-medium', v.temperature && v.temperature > 38.5 ? 'text-clinical-danger' : v.temperature && v.temperature > 37.5 ? 'text-clinical-warning' : '')}>
                        {v.temperature?.toFixed(1) ?? '—'}
                      </Td>
                      <Td>{v.respiratoryRate ?? '—'}</Td>
                      <Td className={cn('font-medium', v.oxygenSaturation && v.oxygenSaturation < 95 ? 'text-clinical-danger' : '')}>
                        {v.oxygenSaturation ? `${v.oxygenSaturation}%` : '—'}
                      </Td>
                      <Td>{v.weightKg ?? '—'}</Td>
                      <Td>{v.bmi?.toFixed(1) ?? '—'}</Td>
                      <Td>{v.criticalAlerts?.length ? <div className="space-y-1">{v.criticalAlerts.map((alert) => <span key={alert} className="block text-xs font-medium text-clinical-danger">{alert}</span>)}</div> : <span className="text-xs text-text-muted">Aucune</span>}</Td>
                      <Td className="text-xs text-text-muted">{v.recordedBy || '—'}</Td>
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
