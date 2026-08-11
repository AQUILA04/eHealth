import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Check, X } from 'lucide-react'
import {
  Badge,
  Button,
  Card,
  CardBody,
  EmptyState,
  Select,
  Spinner,
  Table,
  Td,
  Th,
  Thead,
  Tr,
} from '@/components/ui'
import { signupService, type SignupRequestStatus } from '@/services/subscription.service'
import { format } from 'date-fns'

export default function SignupRequestsPage() {
  const qc = useQueryClient()
  const [status, setStatus] = useState<SignupRequestStatus | ''>('PENDING')

  const { data: requests, isLoading } = useQuery({
    queryKey: ['signup-requests', status],
    queryFn: () => signupService.listRequests(status || undefined),
  })

  const approveMutation = useMutation({
    mutationFn: signupService.approve,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['signup-requests'] }),
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) =>
      signupService.reject(id, reason),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['signup-requests'] }),
  })

  return (
    <div className="space-y-6 p-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900">
            Demandes d’inscription
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            File d’attente : validation manuelle lorsque l’auto-approve Gratuit est désactivé,
            ou pour les offres Clinic / Groupe.
          </p>
        </div>
        <Select
          value={status}
          onChange={(e) => setStatus(e.target.value as SignupRequestStatus | '')}
          className="w-48"
          options={[
            { value: '', label: 'Tous' },
            { value: 'PENDING', label: 'En attente' },
            { value: 'COMPLETED', label: 'Complétées' },
            { value: 'REJECTED', label: 'Rejetées' },
          ]}
        />
      </div>

      <Card>
        <CardBody className="p-0">
          {isLoading ? (
            <div className="flex h-48 items-center justify-center">
              <Spinner />
            </div>
          ) : !requests?.length ? (
            <EmptyState title="Aucune demande" description="La file est vide pour ce filtre." />
          ) : (
            <Table>
              <Thead>
                <Tr>
                  <Th>Organisation</Th>
                  <Th>Admin</Th>
                  <Th>Plan</Th>
                  <Th>Statut</Th>
                  <Th>Date</Th>
                  <Th>Actions</Th>
                </Tr>
              </Thead>
              <tbody>
                {requests.map((req) => (
                  <Tr key={req.id}>
                    <Td>
                      <div className="font-medium">{req.organizationName}</div>
                      {req.tenantId && (
                        <div className="text-xs text-slate-500">tenant: {req.tenantId}</div>
                      )}
                    </Td>
                    <Td>
                      <div>
                        {req.adminFirstName} {req.adminLastName}
                      </div>
                      <div className="text-xs text-slate-500">{req.adminEmail}</div>
                    </Td>
                    <Td>
                      {req.planName}
                      {req.planFree && (
                        <Badge variant="primary" className="ml-2">
                          Gratuit
                        </Badge>
                      )}
                    </Td>
                    <Td>
                      <Badge
                        variant={
                          req.status === 'COMPLETED'
                            ? 'primary'
                            : req.status === 'REJECTED'
                              ? 'danger'
                              : 'warning'
                        }
                      >
                        {req.status}
                      </Badge>
                    </Td>
                    <Td className="text-sm text-slate-500">
                      {format(new Date(req.createdAt), 'dd/MM/yyyy HH:mm')}
                    </Td>
                    <Td>
                      {req.status === 'PENDING' && (
                        <div className="flex gap-2">
                          <Button
                            size="sm"
                            onClick={() => approveMutation.mutate(req.id)}
                            disabled={approveMutation.isPending}
                          >
                            <Check className="mr-1 h-4 w-4" />
                            Approuver
                          </Button>
                          <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => {
                              const reason = window.prompt('Motif du rejet (optionnel)') ?? undefined
                              rejectMutation.mutate({ id: req.id, reason })
                            }}
                            disabled={rejectMutation.isPending}
                          >
                            <X className="mr-1 h-4 w-4" />
                            Rejeter
                          </Button>
                        </div>
                      )}
                    </Td>
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
