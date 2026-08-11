import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Save, Settings2 } from 'lucide-react'
import { Badge, Button, Card, CardBody, CardHeader, Input, Spinner } from '@/components/ui'
import {
  OPERATION_REGISTRY,
  ensurePlanLimits,
  subscriptionService,
  type PlanOperationLimit,
  type SubscriptionPlan,
} from '@/services/subscription.service'

export default function AdminPlansPage() {
  const qc = useQueryClient()
  const { data: plans, isLoading } = useQuery({
    queryKey: ['admin-plans'],
    queryFn: subscriptionService.listPlans,
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<SubscriptionPlan> }) =>
      subscriptionService.updatePlan(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-plans'] }),
  })

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner />
      </div>
    )
  }

  return (
    <div className="space-y-6 p-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-slate-900">Plans d’abonnement</h1>
        <p className="mt-1 text-sm text-slate-500">
          Catalogue, validation automatique du plan Gratuit, et plafonds d’usage.
        </p>
      </div>

      <div className="grid gap-4 lg:grid-cols-1">
        {(plans ?? []).map((plan) => (
          <PlanCard
            key={plan.id}
            plan={plan}
            saving={updateMutation.isPending}
            onSave={(data) => updateMutation.mutateAsync({ id: plan.id, data })}
          />
        ))}
      </div>
    </div>
  )
}

function PlanCard({
  plan,
  onSave,
  saving,
}: {
  plan: SubscriptionPlan
  onSave: (data: Partial<SubscriptionPlan>) => Promise<unknown>
  saving: boolean
}) {
  const [name, setName] = useState(plan.name)
  const [description, setDescription] = useState(plan.description ?? '')
  const [isActive, setIsActive] = useState(plan.isActive)
  const [isPublic, setIsPublic] = useState(plan.isPublic)
  const [autoApprove, setAutoApprove] = useState(plan.autoApproveSignups)
  const [limits, setLimits] = useState(() => ensurePlanLimits(plan.limits as Record<string, unknown>))
  const [expanded, setExpanded] = useState(false)

  const dirty = useMemo(() => {
    return (
      name !== plan.name ||
      description !== (plan.description ?? '') ||
      isActive !== plan.isActive ||
      isPublic !== plan.isPublic ||
      autoApprove !== plan.autoApproveSignups ||
      JSON.stringify(limits) !== JSON.stringify(ensurePlanLimits(plan.limits as Record<string, unknown>))
    )
  }, [name, description, isActive, isPublic, autoApprove, limits, plan])

  const setLimitValue = (key: string, period: string, unlimited: boolean, value?: number) => {
    setLimits((prev) => {
      const next = { ...prev }
      const op: PlanOperationLimit = {
        ...next[key],
        windows: next[key].windows.map((w) =>
          w.period === period
            ? { ...w, limit: unlimited ? null : Math.max(0, value ?? 0) }
            : w
        ),
      }
      next[key] = op
      return next
    })
  }

  return (
    <Card>
      <CardHeader className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-semibold">{plan.name}</h2>
            {plan.isFree && <Badge variant="primary">Gratuit</Badge>}
            {!plan.isActive && <Badge variant="warning">Inactif</Badge>}
          </div>
          <p className="mt-1 text-sm text-slate-500">{plan.description}</p>
        </div>
        <Button
          variant="secondary"
          size="sm"
          onClick={() => setExpanded((v) => !v)}
        >
          <Settings2 className="mr-1 h-4 w-4" />
          {expanded ? 'Masquer' : 'Configurer'}
        </Button>
      </CardHeader>
      {expanded && (
        <CardBody className="space-y-4 border-t border-slate-100">
          <div className="grid gap-3 sm:grid-cols-2">
            <label className="text-sm font-medium text-slate-600">
              Nom
              <Input className="mt-1" value={name} onChange={(e) => setName(e.target.value)} />
            </label>
            <label className="text-sm font-medium text-slate-600">
              Description
              <Input
                className="mt-1"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </label>
          </div>

          <div className="flex flex-wrap gap-4 text-sm">
            <label className="inline-flex items-center gap-2">
              <input type="checkbox" checked={isActive} onChange={(e) => setIsActive(e.target.checked)} />
              Actif
            </label>
            <label className="inline-flex items-center gap-2">
              <input type="checkbox" checked={isPublic} onChange={(e) => setIsPublic(e.target.checked)} />
              Public (visible à l’inscription)
            </label>
            {plan.isFree && (
              <label className="inline-flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={autoApprove}
                  onChange={(e) => setAutoApprove(e.target.checked)}
                />
                Validation automatique des inscriptions Gratuit
              </label>
            )}
          </div>

          <div className="space-y-3">
            <p className="text-sm font-semibold text-slate-800">Limites</p>
            {OPERATION_REGISTRY.map((op) => {
              const config = limits[op.key]
              return (
                <div key={op.key} className="rounded-lg border border-slate-200 p-3">
                  <p className="text-sm font-medium">{op.label}</p>
                  <p className="text-xs text-slate-500">{op.description}</p>
                  <div className="mt-2 space-y-2">
                    {config.windows.map((w) => {
                      const unlimited = w.limit === null
                      return (
                        <div key={w.period} className="flex flex-wrap items-center gap-3">
                          <span className="w-24 text-xs uppercase text-slate-500">{w.period}</span>
                          <label className="inline-flex items-center gap-2 text-sm">
                            <input
                              type="checkbox"
                              checked={unlimited}
                              onChange={(e) =>
                                setLimitValue(op.key, w.period, e.target.checked, w.limit ?? 0)
                              }
                            />
                            Illimité
                          </label>
                          {!unlimited && (
                            <Input
                              type="number"
                              className="w-28"
                              value={w.limit ?? 0}
                              onChange={(e) =>
                                setLimitValue(op.key, w.period, false, Number(e.target.value))
                              }
                            />
                          )}
                        </div>
                      )
                    })}
                  </div>
                </div>
              )
            })}
          </div>

          <div className="flex justify-end">
            <Button
              disabled={!dirty || saving}
              onClick={() =>
                onSave({
                  name,
                  description,
                  isActive,
                  isPublic,
                  autoApproveSignups: autoApprove,
                  limits: limits as unknown as SubscriptionPlan['limits'],
                })
              }
            >
              <Save className="mr-1 h-4 w-4" />
              Enregistrer
            </Button>
          </div>
        </CardBody>
      )}
    </Card>
  )
}
