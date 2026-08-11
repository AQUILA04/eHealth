import { FormEvent, useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useKeycloak } from '@/auth/KeycloakProvider'
import {
  signupService,
  subscriptionService,
  type SignupSubmitResult,
  type SubscriptionPlan,
} from '@/services/subscription.service'

type Step = 'info' | 'plan' | 'done'

export default function SignupPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const { login } = useKeycloak()
  const authEnabled = import.meta.env.VITE_AUTH_ENABLED !== 'false'

  const [step, setStep] = useState<Step>('info')
  const [organization, setOrganization] = useState('')
  const [adminFirstName, setAdminFirstName] = useState('')
  const [adminLastName, setAdminLastName] = useState('')
  const [adminEmail, setAdminEmail] = useState('')
  const [planId, setPlanId] = useState('')
  const [plans, setPlans] = useState<SubscriptionPlan[]>([])
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState<SignupSubmitResult | null>(null)

  useEffect(() => {
    subscriptionService
      .listPublicPlans()
      .then((list) => {
        setPlans(list)
        const preferred =
          list.find((p) => p.id === params.get('plan')) ||
          list.find((p) => p.name.toLowerCase() === (params.get('plan') || '').toLowerCase()) ||
          list.find((p) => p.isFree) ||
          list[0]
        if (preferred) setPlanId(preferred.id)
      })
      .catch(() => setError('Impossible de charger les offres'))
  }, [params])

  const selectedPlan = useMemo(() => plans.find((p) => p.id === planId), [plans, planId])

  const goPlan = () => {
    if (!organization.trim() || !adminFirstName.trim() || !adminLastName.trim() || !adminEmail.trim()) {
      setError('Merci de remplir tous les champs.')
      return
    }
    setError('')
    setStep('plan')
  }

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!planId) {
      setError('Veuillez choisir une offre.')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      const res = await signupService.submit({
        organizationName: organization.trim(),
        adminEmail: adminEmail.trim(),
        adminFirstName: adminFirstName.trim(),
        adminLastName: adminLastName.trim(),
        planId,
      })
      setResult(res)
      setStep('done')
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Impossible de créer la demande. Réessayez.'
      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="landing-root min-h-screen bg-white text-[#0b1c24]">
      <div className="pointer-events-none fixed inset-0 bg-[radial-gradient(ellipse_70%_45%_at_12%_-10%,rgba(11,158,154,0.09),transparent_55%)]" />
      <main className="relative z-10 mx-auto w-full max-w-xl px-5 py-16">
        <Link to="/" className="mb-6 inline-flex text-sm font-medium text-[#6b808a] hover:text-[#087a77]">
          ← Retour à eHealth
        </Link>

        <div className="rounded-2xl border border-[#d9e6ea] bg-white p-7 shadow-[0_24px_60px_rgba(11,28,36,0.08)]">
          <div className="mb-6 flex gap-2" aria-hidden>
            {[0, 1, 2].map((i) => (
              <span
                key={i}
                className={`h-1 flex-1 rounded-full ${
                  (step === 'info' && i === 0) ||
                  (step === 'plan' && i <= 1) ||
                  (step === 'done' && i <= 2)
                    ? 'bg-[#0b9e9a]'
                    : 'bg-[#eef6f7]'
                }`}
              />
            ))}
          </div>

          {step !== 'done' ? (
            <form onSubmit={onSubmit}>
              {step === 'info' && (
                <>
                  <h1 className="font-display text-3xl font-semibold tracking-tight">
                    Créer votre établissement
                  </h1>
                  <p className="mt-2 text-[#3d5560]">
                    Quelques informations pour ouvrir votre espace eHealth et
                    commencer à piloter le parcours patient.
                  </p>

                  <label className="mt-6 block text-sm font-semibold text-[#3d5560]">
                    Nom de l’établissement
                    <input
                      className="mt-1.5 w-full rounded-xl border border-[#d9e6ea] bg-[#f5fafb] px-3.5 py-3 outline-none focus:border-[rgba(11,158,154,0.55)] focus:bg-white focus:ring-4 focus:ring-[rgba(11,158,154,0.12)]"
                      value={organization}
                      onChange={(e) => setOrganization(e.target.value)}
                      placeholder="Clinique des Lilas"
                      required
                    />
                  </label>
                  <div className="mt-4 grid gap-4 sm:grid-cols-2">
                    <label className="block text-sm font-semibold text-[#3d5560]">
                      Prénom
                      <input
                        className="mt-1.5 w-full rounded-xl border border-[#d9e6ea] bg-[#f5fafb] px-3.5 py-3 outline-none focus:border-[rgba(11,158,154,0.55)] focus:bg-white focus:ring-4 focus:ring-[rgba(11,158,154,0.12)]"
                        value={adminFirstName}
                        onChange={(e) => setAdminFirstName(e.target.value)}
                        required
                      />
                    </label>
                    <label className="block text-sm font-semibold text-[#3d5560]">
                      Nom
                      <input
                        className="mt-1.5 w-full rounded-xl border border-[#d9e6ea] bg-[#f5fafb] px-3.5 py-3 outline-none focus:border-[rgba(11,158,154,0.55)] focus:bg-white focus:ring-4 focus:ring-[rgba(11,158,154,0.12)]"
                        value={adminLastName}
                        onChange={(e) => setAdminLastName(e.target.value)}
                        required
                      />
                    </label>
                  </div>
                  <label className="mt-4 block text-sm font-semibold text-[#3d5560]">
                    Email professionnel
                    <input
                      type="email"
                      className="mt-1.5 w-full rounded-xl border border-[#d9e6ea] bg-[#f5fafb] px-3.5 py-3 outline-none focus:border-[rgba(11,158,154,0.55)] focus:bg-white focus:ring-4 focus:ring-[rgba(11,158,154,0.12)]"
                      value={adminEmail}
                      onChange={(e) => setAdminEmail(e.target.value)}
                      required
                    />
                  </label>

                  <button
                    type="button"
                    onClick={goPlan}
                    className="mt-6 inline-flex rounded-full bg-[#0b9e9a] px-5 py-3 text-sm font-semibold text-white hover:bg-[#087a77]"
                  >
                    Continuer
                  </button>
                </>
              )}

              {step === 'plan' && (
                <>
                  <h1 className="font-display text-3xl font-semibold tracking-tight">
                    Choisir une offre
                  </h1>
                  <p className="mt-2 text-[#3d5560]">
                    Commencez gratuitement pour découvrir le produit, ou
                    sélectionnez une offre adaptée à votre structure.
                  </p>

                  <div className="mt-6 space-y-3">
                    {plans.map((plan) => (
                      <label
                        key={plan.id}
                        className={`relative block cursor-pointer rounded-xl border py-4 pl-11 pr-20 ${
                          planId === plan.id
                            ? 'border-[rgba(11,158,154,0.55)] bg-[rgba(11,158,154,0.06)]'
                            : 'border-[#d9e6ea]'
                        }`}
                      >
                        <input
                          type="radio"
                          className="absolute left-4 top-5 accent-[#0b9e9a]"
                          name="plan"
                          checked={planId === plan.id}
                          onChange={() => setPlanId(plan.id)}
                        />
                        <strong className="block">{plan.name}</strong>
                        <span className="absolute right-4 top-4 font-display font-semibold text-[#087a77]">
                          {plan.isFree ? '0 €' : 'Devis'}
                        </span>
                        <span className="mt-1 block text-sm text-[#3d5560]">
                          {plan.description}
                        </span>
                      </label>
                    ))}
                  </div>

                  <div className="mt-6 flex flex-wrap gap-3">
                    <button
                      type="button"
                      onClick={() => setStep('info')}
                      className="inline-flex rounded-full border border-[#d9e6ea] px-5 py-3 text-sm font-semibold text-[#3d5560]"
                    >
                      Retour
                    </button>
                    <button
                      type="submit"
                      disabled={submitting || !planId}
                      className="inline-flex rounded-full bg-[#0b9e9a] px-5 py-3 text-sm font-semibold text-white hover:bg-[#087a77] disabled:opacity-60"
                    >
                      {submitting ? 'Création…' : 'Créer mon espace'}
                    </button>
                  </div>
                </>
              )}

              {error && <p className="mt-4 text-sm text-[#b42318]">{error}</p>}
            </form>
          ) : (
            <div className="text-center">
              <h1 className="font-display text-3xl font-semibold tracking-tight">
                {result?.provisioned ? 'C’est parti' : 'Demande enregistrée'}
              </h1>
              <p className="mt-3 text-[#3d5560]">{result?.message}</p>
              {result?.provisioned && result.temporaryPassword && (
                <p className="mt-3 rounded-xl bg-[#f5fafb] px-4 py-3 text-sm text-[#087a77]">
                  Mot de passe temporaire : <strong>{result.temporaryPassword}</strong>
                  <br />
                  <span className="text-[#6b808a]">
                    Connectez-vous avec {adminEmail} (changement demandé à la première connexion).
                  </span>
                </p>
              )}
              {!result?.provisioned && selectedPlan && (
                <p className="mt-3 text-sm text-[#6b808a]">
                  Offre sélectionnée : {selectedPlan.name}. Vous serez notifié après validation.
                </p>
              )}
              <div className="mt-8 flex flex-wrap justify-center gap-3">
                {result?.provisioned ? (
                  <button
                    type="button"
                    onClick={() => (authEnabled ? login() : navigate('/dashboard'))}
                    className="inline-flex rounded-full bg-[#0b9e9a] px-6 py-3.5 text-base font-semibold text-white hover:bg-[#087a77]"
                  >
                    Se connecter à mon espace
                  </button>
                ) : (
                  <Link
                    to="/"
                    className="inline-flex rounded-full bg-[#0b9e9a] px-6 py-3.5 text-base font-semibold text-white hover:bg-[#087a77]"
                  >
                    Retour à l’accueil
                  </Link>
                )}
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  )
}
