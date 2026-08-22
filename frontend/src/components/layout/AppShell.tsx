import React, { useState } from 'react'
import { NavLink, useLocation, Outlet } from 'react-router-dom'
import { cn } from '@/components/ui'
import { useKeycloak } from '@/auth/KeycloakProvider'
import { PERMISSIONS, type Permission, usePermissions } from '@/auth/permissions'
import {
  LayoutDashboard,
  Users,
  BedDouble,
  CalendarDays,
  FileText,
  Activity,
  Pill,
  FlaskConical,
  ChevronLeft,
  Menu,
  Bell,
  LogOut,
  User,
  Stethoscope,
  Building2,
  Shield,
  Settings,
  ClipboardList,
  Droplets,
  ScanLine,
  WalletCards,
  UsersRound,
  Utensils,
  ChartNoAxesCombined,
} from 'lucide-react'

interface NavItem {
  label: string
  to: string
  icon: React.ReactNode
  roles?: string[]
  permission?: Permission
}

interface NavSection {
  title: string
  items: NavItem[]
}

const NAV_SECTIONS: NavSection[] = [
  {
    title: 'Tableau de bord',
    items: [
      { label: 'Vue d\'ensemble', to: '/dashboard', icon: <LayoutDashboard className="h-4 w-4" /> },
    ],
  },
  {
    title: 'Module I — GAP',
    items: [
      { label: 'Patients', to: '/gap/patients', icon: <Users className="h-4 w-4" />, roles: ['ADMIN_GAP'] },
      { label: 'Admissions (ADT)', to: '/gap/encounters', icon: <BedDouble className="h-4 w-4" />, roles: ['ADMIN_GAP'] },
      { label: 'Tableau des lits', to: '/gap/bed-board', icon: <Building2 className="h-4 w-4" />, roles: ['ADMIN_GAP'] },
      { label: 'Rendez-vous', to: '/gap/appointments', icon: <CalendarDays className="h-4 w-4" />, roles: ['ADMIN_GAP'] },
    ],
  },
  {
    title: 'Module II — DPI',
    items: [
      { label: 'Dossiers cliniques', to: '/dpi/encounters', icon: <FileText className="h-4 w-4" />, roles: ['MEDECIN', 'INFIRMIER'] },
      { label: 'Constantes vitales', to: '/dpi/vitals', icon: <Activity className="h-4 w-4" />, roles: ['MEDECIN', 'INFIRMIER'] },
      { label: 'Prescriptions (CPOE)', to: '/dpi/medications', icon: <Pill className="h-4 w-4" />, roles: ['MEDECIN', 'INFIRMIER'] },
      { label: 'Examens de labo', to: '/dpi/lab-orders', icon: <FlaskConical className="h-4 w-4" />, roles: ['MEDECIN', 'INFIRMIER'] },
    ],
  },
  {
    title: 'Module III — Plateaux techniques',
    items: [
      { label: 'Laboratoire (LIS)', to: '/lis/worklist', icon: <FlaskConical className="h-4 w-4" />, permission: PERMISSIONS.LIS_WORKLIST_VIEW },
      { label: 'Banque de sang', to: '/lis/blood-bank', icon: <Droplets className="h-4 w-4" />, permission: PERMISSIONS.BLOOD_BANK_VIEW },
      { label: 'Radiologie (RIS)', to: '/ris/worklist', icon: <ScanLine className="h-4 w-4" />, permission: PERMISSIONS.RIS_WORKLIST_VIEW },
    ],
  },
  {
    title: 'Module IV — Pharmacie',
    items: [
      { label: 'Stocks et dispensation', to: '/pharmacy', icon: <Pill className="h-4 w-4" />, permission: PERMISSIONS.PHARMACY_VIEW },
    ],
  },
  {
    title: 'Module V — Cycle de revenus',
    items: [
      { label: 'Facturation et caisse', to: '/rcm', icon: <WalletCards className="h-4 w-4" />, permission: PERMISSIONS.RCM_VIEW },
    ],
  },
  {
    title: 'Module VI — Ressources humaines',
    items: [
      { label: 'Personnel et planning', to: '/hr', icon: <UsersRound className="h-4 w-4" />, permission: PERMISSIONS.HR_VIEW },
    ],
  },
  {
    title: 'Module VII — Services de support',
    items: [
      { label: 'Hôtellerie et maintenance', to: '/support', icon: <Utensils className="h-4 w-4" />, permission: PERMISSIONS.SUPPORT_VIEW },
    ],
  },
  {
    title: 'Module IX — Business Intelligence',
    items: [
      { label: 'Pilotage et analytique', to: '/analytics', icon: <ChartNoAxesCombined className="h-4 w-4" />, permission: PERMISSIONS.ANALYTICS_VIEW },
    ],
  },
  {
    title: 'Administration',
    items: [
      { label: 'Gestion des tenants', to: '/admin/tenants', icon: <Shield className="h-4 w-4" />, roles: ['SUPER_ADMIN', 'ADMIN_SYSTEM'] },
      { label: 'Plans', to: '/admin/plans', icon: <Settings className="h-4 w-4" />, roles: ['SUPER_ADMIN', 'ADMIN_SYSTEM'] },
      { label: 'Demandes d’inscription', to: '/admin/signup-requests', icon: <ClipboardList className="h-4 w-4" />, roles: ['SUPER_ADMIN', 'ADMIN_SYSTEM'] },
    ],
  },
]

export default function AppShell() {
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const { userInfo, logout, hasRole } = useKeycloak()
  const { can } = usePermissions()
  const location = useLocation()

  const filteredSections = NAV_SECTIONS.map((section) => {
    const items = section.items.filter((item) => {
      if (item.permission && !can(item.permission)) return false
      if (!item.roles || item.roles.length === 0) return true
      return item.roles.some((r) => hasRole(r))
    })
    return { ...section, items }
  }).filter((section) => section.items.length > 0)

  const sidebarContent = (
    <aside
      className={cn(
        'flex h-full flex-col bg-surface-sidebar text-text-inverse transition-all duration-200',
        collapsed ? 'w-16' : 'w-[260px]'
      )}
    >
      {/* Logo */}
      <div className="flex h-16 items-center justify-between border-b border-white/10 px-4">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-600">
              <Stethoscope className="h-4 w-4 text-white" />
            </div>
            <div>
              <p className="text-sm font-bold text-white leading-tight">eHealth SIH</p>
              <p className="text-2xs text-slate-400 leading-tight">v0.3.0</p>
            </div>
          </div>
        )}
        {collapsed && (
          <div className="mx-auto flex h-8 w-8 items-center justify-center rounded-lg bg-primary-600">
            <Stethoscope className="h-4 w-4 text-white" />
          </div>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="hidden lg:flex rounded-lg p-1.5 text-slate-400 hover:bg-white/10 hover:text-white transition-colors cursor-pointer"
          aria-label={collapsed ? 'Développer le menu' : 'Réduire le menu'}
        >
          <ChevronLeft className={cn('h-4 w-4 transition-transform', collapsed && 'rotate-180')} />
        </button>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto scrollbar-thin py-4 px-2">
        {filteredSections.map((section) => (
          <div key={section.title} className="mb-4">
            {!collapsed && (
              <p className="mb-1 px-3 text-2xs font-semibold uppercase tracking-widest text-slate-500">
                {section.title}
              </p>
            )}
            {section.items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors duration-100',
                    isActive
                      ? 'bg-primary-600 text-white'
                      : 'text-slate-300 hover:bg-white/10 hover:text-white',
                    collapsed && 'justify-center px-2'
                  )
                }
                title={collapsed ? item.label : undefined}
                onClick={() => setMobileOpen(false)}
              >
                {item.icon}
                {!collapsed && <span>{item.label}</span>}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>

      {/* User footer */}
      <div className="border-t border-white/10 p-3">
        <div className={cn('flex items-center gap-3', collapsed && 'justify-center')}>
          <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-primary-600">
            <User className="h-4 w-4 text-white" />
          </div>
          {!collapsed && (
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium text-white">{userInfo?.fullName || userInfo?.username}</p>
              <p className="truncate text-2xs text-slate-400">{userInfo?.roles?.[0] || 'Utilisateur'}</p>
            </div>
          )}
          {!collapsed && (
            <button
              onClick={logout}
              className="rounded-lg p-1.5 text-slate-400 hover:bg-white/10 hover:text-white transition-colors cursor-pointer"
              aria-label="Se déconnecter"
            >
              <LogOut className="h-4 w-4" />
            </button>
          )}
        </div>
      </div>
    </aside>
  )

  return (
    <div className="flex h-screen overflow-hidden bg-surface">
      {/* Sidebar desktop */}
      <div className="hidden lg:flex h-full flex-shrink-0">{sidebarContent}</div>

      {/* Sidebar mobile overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => setMobileOpen(false)}
            aria-hidden="true"
          />
          <div className="absolute left-0 top-0 h-full z-50">{sidebarContent}</div>
        </div>
      )}

      {/* Main content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Header */}
        <header className="flex h-16 flex-shrink-0 items-center justify-between border-b border-surface-border bg-white px-4 shadow-sm">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setMobileOpen(true)}
              className="lg:hidden rounded-lg p-2 text-text-muted hover:bg-slate-100 cursor-pointer"
              aria-label="Ouvrir le menu"
            >
              <Menu className="h-5 w-5" />
            </button>
            <Breadcrumb pathname={location.pathname} />
          </div>
          <div className="flex items-center gap-2">
            <button className="relative rounded-lg p-2 text-text-muted hover:bg-slate-100 transition-colors cursor-pointer">
              <Bell className="h-5 w-5" />
              <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-clinical-danger" />
            </button>
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 text-primary-700 text-sm font-semibold">
              {(userInfo?.fullName || userInfo?.username || 'U').charAt(0).toUpperCase()}
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto scrollbar-thin p-4 md:p-6 animate-fade-in">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

function Breadcrumb({ pathname }: { pathname: string }) {
  const segments = pathname.split('/').filter(Boolean)
  const labels: Record<string, string> = {
    gap: 'GAP',
    dpi: 'DPI',
    patients: 'Patients',
    encounters: 'Admissions',
    'bed-board': 'Tableau des lits',
    appointments: 'Rendez-vous',
    vitals: 'Constantes vitales',
    medications: 'Prescriptions',
    'lab-orders': 'Examens de labo',
    lis: 'Laboratoire',
    worklist: 'Poste de travail',
    'blood-bank': 'Banque de sang',
    ris: 'Radiologie',
    pharmacy: 'Pharmacie',
    hr: 'Ressources humaines',
    support: 'Services de support',
    analytics: 'Pilotage et analytique',
  }

  if (segments.length === 0) return <h1 className="text-sm font-semibold text-text-primary">Vue d'ensemble</h1>

  return (
    <nav aria-label="Fil d'Ariane">
      <ol className="flex items-center gap-1 text-sm">
        {segments.map((seg, i) => (
          <React.Fragment key={seg}>
            {i > 0 && <span className="text-text-muted">/</span>}
            <li
              className={cn(
                'font-medium',
                i === segments.length - 1 ? 'text-text-primary' : 'text-text-muted'
              )}
            >
              {labels[seg] || seg}
            </li>
          </React.Fragment>
        ))}
      </ol>
    </nav>
  )
}
