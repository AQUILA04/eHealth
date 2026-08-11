import { Link } from 'react-router-dom'
import { useKeycloak } from '@/auth/KeycloakProvider'
import { Activity } from 'lucide-react'

const features = [
  {
    title: 'Identité patient fiable',
    description:
      'Une seule fiche, partagée entre services. Moins de doublons, moins d’erreurs, plus de confiance au lit du patient.',
  },
  {
    title: 'Admissions & lits maîtrisés',
    description:
      'Admission, transfert, sortie et occupation des lits lisibles en un coup d’œil — pour anticiper plutôt que subir.',
  },
  {
    title: 'Dossier clinique vivant',
    description:
      'Observations, prescriptions et biologie structurées, prêtes pour le soin et pour l’échange sécurisé.',
  },
  {
    title: 'Rendez-vous fluides',
    description:
      'Planifiez et suivez les consultations sans spreadsheets ni allers-retours entre secrétariat et services.',
  },
  {
    title: 'Équipes coordonnées',
    description:
      'Médecins, infirmiers et administratifs travaillent sur la même réalité — avec des droits adaptés à chaque rôle.',
  },
  {
    title: 'Données protégées',
    description:
      'Accès contrôlés, historique des actions, isolation des établissements : la confiance n’est pas une option.',
  },
]

const benefits = [
  'Réduisez les doublons et les erreurs d’identité',
  'Accélérez admissions, transferts et sorties',
  'Gagnez en clarté sur l’occupation des lits',
  'Centralisez le dossier pour toute l’équipe soignante',
  'Sécurisez les accès avec une traçabilité complète',
  'Pilotez plusieurs sites avec la même exigence de soin',
]

const steps = [
  {
    n: '01',
    t: 'Accueillir',
    d: 'Identifiez le patient, ouvrez l’admission, attribuez un lit — en quelques gestes, sans ressaisie.',
  },
  {
    n: '02',
    t: 'Soigner',
    d: 'Documentez le séjour : constantes, prescriptions, examens. L’équipe partage la même vue clinique.',
  },
  {
    n: '03',
    t: 'Transmettre',
    d: 'À la sortie ou au transfert, l’essentiel du dossier voyage avec le patient — clairement et en sécurité.',
  },
]

const stats = [
  { value: '1', label: 'identité patient unique' },
  { value: '0', label: 'rupture entre services' },
  { value: '24/7', label: 'visibilité des lits & flux' },
  { value: '100%', label: 'traçabilité des accès' },
]

export default function LandingPage() {
  const { login, isAuthenticated } = useKeycloak()

  const onLogin = () => {
    const authEnabled = import.meta.env.VITE_AUTH_ENABLED !== 'false'
    if (!authEnabled || isAuthenticated) {
      window.location.href = '/dashboard'
      return
    }
    login()
  }

  return (
    <div className="landing-root min-h-screen bg-white text-[#0b1c24] antialiased">
      <div className="pointer-events-none fixed inset-0 bg-[radial-gradient(ellipse_70%_45%_at_12%_-10%,rgba(11,158,154,0.09),transparent_55%),radial-gradient(ellipse_50%_40%_at_95%_8%,rgba(180,220,230,0.35),transparent_50%)]" />

      <header className="sticky top-0 z-40 border-b border-[#d9e6ea]/80 bg-white/80 backdrop-blur-md">
        <div className="mx-auto flex h-[4.25rem] max-w-6xl items-center justify-between gap-4 px-5">
          <Link to="/" className="flex items-center gap-2.5 text-[#087a77]">
            <span className="flex h-9 w-9 items-center justify-center rounded-xl border border-[#d9e6ea]">
              <Activity className="h-4 w-4" />
            </span>
            <span className="leading-tight">
              <span className="block font-display text-lg font-bold tracking-tight text-[#0b1c24]">
                eHealth
              </span>
              <span className="block text-[0.68rem] font-medium uppercase tracking-wider text-[#6b808a]">
                by OptimizeSolux
              </span>
            </span>
          </Link>

          <nav className="hidden items-center gap-7 md:flex">
            <a href="#produit" className="text-sm font-medium text-[#3d5560] hover:text-[#087a77]">
              Produit
            </a>
            <a href="#avantages" className="text-sm font-medium text-[#3d5560] hover:text-[#087a77]">
              Avantages
            </a>
            <a href="#parcours" className="text-sm font-medium text-[#3d5560] hover:text-[#087a77]">
              Parcours
            </a>
          </nav>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={onLogin}
              className="hidden rounded-full border border-[#d9e6ea] px-4 py-2 text-sm font-semibold text-[#3d5560] hover:border-[#c2d5db] hover:text-[#0b1c24] sm:inline-flex"
            >
              Se connecter
            </button>
            <Link
              to="/signup"
              className="inline-flex rounded-full bg-[#0b9e9a] px-4 py-2 text-sm font-semibold text-white shadow-[0_10px_28px_rgba(11,158,154,0.22)] hover:bg-[#087a77]"
            >
              Commencer gratuitement
            </Link>
          </div>
        </div>
      </header>

      <main className="relative z-10">
        <section className="relative overflow-hidden px-5 pb-14 pt-16 md:pt-20">
          <div className="mx-auto grid max-w-6xl items-center gap-10 lg:grid-cols-2">
            <div className="max-w-xl">
              <p className="font-display text-5xl font-bold tracking-tight text-[#0b1c24] md:text-6xl">
                eHealth
              </p>
              <h1 className="mt-4 font-display text-3xl font-semibold leading-tight tracking-tight md:text-4xl">
                Le soin commence
                <br />
                par une information nette.
              </h1>
              <p className="mt-4 text-lg leading-relaxed text-[#3d5560]">
                eHealth relie l’identité patient, les admissions et le dossier
                clinique — pour des équipes plus sereines et un parcours sans
                rupture.
              </p>
              <div className="mt-8 flex flex-wrap items-center gap-3">
                <Link
                  to="/signup"
                  className="inline-flex rounded-full bg-[#0b9e9a] px-6 py-3.5 text-base font-semibold text-white shadow-[0_10px_28px_rgba(11,158,154,0.22)] hover:bg-[#087a77]"
                >
                  Commencer gratuitement
                </Link>
                <button
                  type="button"
                  onClick={onLogin}
                  className="inline-flex rounded-full px-5 py-3.5 text-base font-semibold text-[#3d5560] hover:text-[#087a77]"
                >
                  Se connecter
                </button>
              </div>
              <p className="mt-4 text-sm text-[#6b808a]">
                Déjà équipé&nbsp;? Connectez-vous pour accéder à votre espace.
              </p>
            </div>

            <div className="rounded-2xl border border-[#d9e6ea] bg-white/90 shadow-[0_24px_60px_rgba(11,28,36,0.08)] backdrop-blur">
              <div className="flex items-center gap-2 border-b border-[#d9e6ea] bg-[#f5fafb] px-4 py-3">
                <span className="h-2 w-2 rounded-full bg-[#f0a8a0]" />
                <span className="h-2 w-2 rounded-full bg-[#c2d5db]" />
                <span className="h-2 w-2 rounded-full bg-[#c2d5db]" />
                <span className="ml-auto text-[0.7rem] font-semibold uppercase tracking-wider text-[#6b808a]">
                  Vue clinique · temps réel
                </span>
              </div>
              <div className="grid grid-cols-[52px_1fr]">
                <div className="space-y-2 border-r border-[#d9e6ea] bg-[#fcfefe] p-3">
                  <div className="h-7 rounded-lg bg-[rgba(11,158,154,0.12)] ring-1 ring-[rgba(11,158,154,0.25)]" />
                  <div className="h-7 rounded-lg bg-[#eef6f7]" />
                  <div className="h-7 rounded-lg bg-[#eef6f7]" />
                </div>
                <div className="space-y-4 p-4">
                  <div className="grid grid-cols-3 gap-2">
                    {[
                      { k: 'FC', v: '72', s: 'bpm' },
                      { k: 'SpO₂', v: '98', s: '%' },
                      { k: 'TA', v: '118/76', s: 'mmHg' },
                    ].map((item) => (
                      <div key={item.k} className="rounded-xl bg-[#f5fafb] px-3 py-2.5">
                        <div className="text-[0.68rem] font-semibold uppercase tracking-wider text-[#6b808a]">
                          {item.k}
                        </div>
                        <div className="font-display text-xl font-semibold">{item.v}</div>
                        <div className="text-xs text-[#6b808a]">{item.s}</div>
                      </div>
                    ))}
                  </div>
                  <div className="h-16 rounded-xl bg-gradient-to-b from-[rgba(11,158,154,0.08)] to-transparent" />
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="border-y border-[#d9e6ea] bg-[#f5fafb] py-8">
          <div className="mx-auto grid max-w-6xl grid-cols-2 gap-6 px-5 md:grid-cols-4">
            {stats.map((stat) => (
              <div key={stat.label} className="text-center">
                <div className="font-display text-3xl font-semibold tracking-tight text-[#087a77]">
                  {stat.value}
                </div>
                <div className="mt-1 text-sm text-[#6b808a]">{stat.label}</div>
              </div>
            ))}
          </div>
        </section>

        <section id="produit" className="py-20">
          <div className="mx-auto max-w-6xl px-5">
            <div className="max-w-xl">
              <h2 className="font-display text-3xl font-semibold tracking-tight md:text-4xl">
                Tout ce dont l’hôpital a besoin, au même endroit
              </h2>
              <p className="mt-3 text-lg text-[#3d5560]">
                Une plateforme pensée pour le terrain : accueil, services de
                soins et direction — avec une information qui suit le patient.
              </p>
            </div>
            <div className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {features.map((feature) => (
                <article
                  key={feature.title}
                  className="rounded-2xl border border-[#d9e6ea] bg-white p-6 transition-shadow hover:shadow-[0_16px_40px_rgba(11,28,36,0.06)]"
                >
                  <h3 className="text-lg font-semibold tracking-tight">{feature.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-[#3d5560]">{feature.description}</p>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section id="parcours" className="border-t border-[#d9e6ea] bg-[#f7fbfc] py-20">
          <div className="mx-auto max-w-6xl px-5">
            <div className="max-w-xl">
              <h2 className="font-display text-3xl font-semibold tracking-tight md:text-4xl">
                Du seuil de l’hôpital au lit du patient
              </h2>
              <p className="mt-3 text-lg text-[#3d5560]">
                Un fil conducteur d’information — sans rupture entre le guichet,
                le service et le dossier.
              </p>
            </div>
            <ol className="mt-10 divide-y divide-[#d9e6ea] border-y border-[#d9e6ea]">
              {steps.map((step) => (
                <li
                  key={step.n}
                  className="grid gap-2 py-6 md:grid-cols-[5rem_12rem_1fr] md:items-baseline md:gap-6"
                >
                  <span className="font-display text-2xl font-semibold text-[#0b9e9a]">{step.n}</span>
                  <h3 className="text-lg font-semibold">{step.t}</h3>
                  <p className="text-[#3d5560]">{step.d}</p>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section id="avantages" className="border-t border-[#d9e6ea] bg-[#0b1c24] py-20 text-white">
          <div className="mx-auto grid max-w-6xl gap-12 px-5 lg:grid-cols-2 lg:items-center">
            <div>
              <h2 className="font-display text-3xl font-semibold tracking-tight md:text-4xl">
                Pourquoi les établissements choisissent eHealth
              </h2>
              <p className="mt-4 text-lg text-white/60">
                Moins de friction administrative. Plus de temps clinique. Une
                vision unique du patient, d’un service à l’autre.
              </p>
            </div>
            <ul className="space-y-4">
              {benefits.map((benefit) => (
                <li key={benefit} className="flex items-start gap-3 text-white/80">
                  <span className="mt-2 h-2 w-2 shrink-0 rounded-sm bg-[#0b9e9a]" />
                  {benefit}
                </li>
              ))}
            </ul>
          </div>
        </section>

        <section className="px-5 py-20">
          <div className="mx-auto max-w-6xl rounded-3xl border border-[#d9e6ea] bg-gradient-to-br from-[rgba(11,158,154,0.1)] to-[#f5fafb] px-8 py-14 text-center">
            <h2 className="font-display text-3xl font-semibold tracking-tight md:text-4xl">
              Prêt à moderniser le parcours patient&nbsp;?
            </h2>
            <p className="mx-auto mt-3 max-w-xl text-lg text-[#3d5560]">
              Ouvrez votre espace eHealth et découvrez une information
              hospitalière claire, du premier accueil au dossier clinique.
            </p>
            <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
              <Link
                to="/signup"
                className="inline-flex rounded-full bg-[#0b9e9a] px-6 py-3.5 text-base font-semibold text-white hover:bg-[#087a77]"
              >
                Commencer gratuitement
              </Link>
              <button
                type="button"
                onClick={onLogin}
                className="inline-flex rounded-full px-5 py-3.5 text-base font-semibold text-[#3d5560] hover:text-[#087a77]"
              >
                Se connecter
              </button>
            </div>
          </div>
        </section>
      </main>

      <footer className="border-t border-[#d9e6ea] bg-white py-10">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 px-5 md:flex-row md:items-end md:justify-between">
          <div>
            <div className="font-display text-xl font-semibold">eHealth</div>
            <div className="text-xs uppercase tracking-wider text-[#6b808a]">by OptimizeSolux</div>
          </div>
          <div className="flex flex-wrap gap-4 text-sm text-[#3d5560]">
            <button type="button" onClick={onLogin} className="hover:text-[#087a77]">
              Se connecter
            </button>
            <Link to="/signup" className="hover:text-[#087a77]">
              Commencer gratuitement
            </Link>
            <a href="mailto:ehealth@optimizesolux.com" className="hover:text-[#087a77]">
              ehealth@optimizesolux.com
            </a>
          </div>
        </div>
      </footer>
    </div>
  )
}
