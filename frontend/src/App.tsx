import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { KeycloakProvider, useKeycloak } from '@/auth/KeycloakProvider'
import { PERMISSIONS, type Permission, usePermissions } from '@/auth/permissions'
import AppShell from '@/components/layout/AppShell'
import Dashboard from '@/pages/Dashboard'
import PatientsPage from '@/pages/gap/PatientsPage'
import EncountersPage from '@/pages/gap/EncountersPage'
import BedBoardPage from '@/pages/gap/BedBoardPage'
import AppointmentsPage from '@/pages/gap/AppointmentsPage'
import ClinicalEncountersPage from '@/pages/dpi/ClinicalEncountersPage'
import ClinicalEncounterDetailPage from '@/pages/dpi/ClinicalEncounterDetailPage'
import VitalsPage from '@/pages/dpi/VitalsPage'
import MedicationsPage from '@/pages/dpi/MedicationsPage'
import LabOrdersPage from '@/pages/dpi/LabOrdersPage'
import TenantsPage from '@/pages/admin/TenantsPage'
import AdminPlansPage from '@/pages/admin/AdminPlansPage'
import SignupRequestsPage from '@/pages/admin/SignupRequestsPage'
import LandingPage from '@/pages/LandingPage'
import SignupPage from '@/pages/SignupPage'
import LaboratoryWorklistPage from '@/pages/lis/LaboratoryWorklistPage'
import BloodBankPage from '@/pages/lis/BloodBankPage'
import RadiologyWorklistPage from '@/pages/ris/RadiologyWorklistPage'
import PharmacyDashboardPage from '@/pages/pharmacy/PharmacyDashboardPage'
import RevenueCyclePage from '@/pages/rcm/RevenueCyclePage'
import HumanResourcesPage from '@/pages/hr/HumanResourcesPage'
import SupportOperationsPage from '@/pages/support/SupportOperationsPage'
import PatientPortalPage from '@/pages/portal/PatientPortalPage'
import AnalyticsDashboardPage from '@/pages/analytics/AnalyticsDashboardPage'
import SmartQueuePage from '@/pages/gap/SmartQueuePage'
import PublicQueueDisplayPage from '@/pages/gap/PublicQueueDisplayPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
})

function PermissionRoute({ children, permission }: { children: React.ReactNode; permission: Permission }) {
  const { can, isLoading } = usePermissions()
  if (isLoading) return null
  if (!can(permission)) return <Navigate to="/dashboard" replace />
  return <>{children}</>
}

function RoleRoute({ children, allowedRoles }: { children: React.ReactNode; allowedRoles: string[] }) {
  const { hasRole, isLoading } = useKeycloak()
  if (isLoading) return null
  const authorized = allowedRoles.some((r) => hasRole(r))
  if (!authorized) {
    return <Navigate to="/dashboard" replace />
  }
  return <>{children}</>
}

/** Requires an authenticated session; triggers Keycloak login otherwise. */
function RequireAuth() {
  const { isAuthenticated, isLoading, login } = useKeycloak()
  const authEnabled = import.meta.env.VITE_AUTH_ENABLED !== 'false'

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center bg-slate-50">
        <div className="h-10 w-10 animate-spin rounded-full border-4 border-primary-600 border-t-transparent" />
      </div>
    )
  }

  if (authEnabled && !isAuthenticated) {
    login()
    return (
      <div className="flex h-screen items-center justify-center bg-slate-50">
        <p className="text-sm text-slate-500">Redirection vers la connexion…</p>
      </div>
    )
  }

  return <Outlet />
}

function HomeEntry() {
  const { isAuthenticated, isLoading } = useKeycloak()
  if (isLoading) return null
  // With Keycloak enabled, signed-in users go straight to the app.
  // In no-auth/dev, keep the public product home visible (CleanTrack-style).
  const authEnabled = import.meta.env.VITE_AUTH_ENABLED !== 'false'
  if (authEnabled && isAuthenticated) return <Navigate to="/dashboard" replace />
  return <LandingPage />
}

function LoginEntry() {
  const { isAuthenticated, isLoading, login } = useKeycloak()
  if (isLoading) return null
  if (isAuthenticated) return <Navigate to="/dashboard" replace />
  const authEnabled = import.meta.env.VITE_AUTH_ENABLED !== 'false'
  if (!authEnabled) return <Navigate to="/dashboard" replace />
  login()
  return (
    <div className="flex h-screen items-center justify-center bg-white">
      <p className="text-sm text-[#6b808a]">Ouverture de la connexion…</p>
    </div>
  )
}

export default function App() {
  return (
    <KeycloakProvider>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<HomeEntry />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/login" element={<LoginEntry />} />

            <Route element={<RequireAuth />}>
              <Route element={<AppShell />}>
                <Route path="/dashboard" element={<Dashboard />} />

                <Route path="/gap/patients" element={<RoleRoute allowedRoles={['ADMIN_GAP']}><PatientsPage /></RoleRoute>} />
                <Route path="/gap/encounters" element={<RoleRoute allowedRoles={['ADMIN_GAP']}><EncountersPage /></RoleRoute>} />
                <Route path="/gap/bed-board" element={<RoleRoute allowedRoles={['ADMIN_GAP']}><BedBoardPage /></RoleRoute>} />
                <Route path="/gap/appointments" element={<RoleRoute allowedRoles={['ADMIN_GAP']}><AppointmentsPage /></RoleRoute>} />

                <Route path="/dpi/encounters" element={<RoleRoute allowedRoles={['MEDECIN', 'INFIRMIER']}><ClinicalEncountersPage /></RoleRoute>} />
                <Route path="/dpi/encounters/:id" element={<RoleRoute allowedRoles={['MEDECIN', 'INFIRMIER']}><ClinicalEncounterDetailPage /></RoleRoute>} />
                <Route path="/dpi/vitals" element={<RoleRoute allowedRoles={['MEDECIN', 'INFIRMIER']}><VitalsPage /></RoleRoute>} />
                <Route path="/dpi/medications" element={<RoleRoute allowedRoles={['MEDECIN', 'INFIRMIER']}><MedicationsPage /></RoleRoute>} />
                <Route path="/dpi/lab-orders" element={<RoleRoute allowedRoles={['MEDECIN', 'INFIRMIER']}><LabOrdersPage /></RoleRoute>} />

                <Route path="/lis/worklist" element={<PermissionRoute permission={PERMISSIONS.LIS_WORKLIST_VIEW}><LaboratoryWorklistPage /></PermissionRoute>} />
                <Route path="/lis/blood-bank" element={<PermissionRoute permission={PERMISSIONS.BLOOD_BANK_VIEW}><BloodBankPage /></PermissionRoute>} />
                <Route path="/ris/worklist" element={<PermissionRoute permission={PERMISSIONS.RIS_WORKLIST_VIEW}><RadiologyWorklistPage /></PermissionRoute>} />
                <Route path="/pharmacy" element={<PermissionRoute permission={PERMISSIONS.PHARMACY_VIEW}><PharmacyDashboardPage /></PermissionRoute>} />
                <Route path="/rcm" element={<PermissionRoute permission={PERMISSIONS.RCM_VIEW}><RevenueCyclePage /></PermissionRoute>} />
                <Route path="/hr" element={<PermissionRoute permission={PERMISSIONS.HR_VIEW}><HumanResourcesPage /></PermissionRoute>} />
                <Route path="/support" element={<PermissionRoute permission={PERMISSIONS.SUPPORT_VIEW}><SupportOperationsPage /></PermissionRoute>} />
                <Route path="/portal" element={<PermissionRoute permission={PERMISSIONS.PORTAL_VIEW}><PatientPortalPage /></PermissionRoute>} />
                <Route path="/analytics" element={<PermissionRoute permission={PERMISSIONS.ANALYTICS_VIEW}><AnalyticsDashboardPage /></PermissionRoute>} />
                <Route path="/gap/smart-queue" element={<PermissionRoute permission={PERMISSIONS.SMART_QUEUE_VIEW}><SmartQueuePage /></PermissionRoute>} />
                <Route path="/queue-display" element={<PublicQueueDisplayPage />} />

                <Route path="/admin/tenants" element={<RoleRoute allowedRoles={['SUPER_ADMIN', 'ADMIN_SYSTEM']}><TenantsPage /></RoleRoute>} />
                <Route path="/admin/plans" element={<RoleRoute allowedRoles={['SUPER_ADMIN', 'ADMIN_SYSTEM']}><AdminPlansPage /></RoleRoute>} />
                <Route path="/admin/signup-requests" element={<RoleRoute allowedRoles={['SUPER_ADMIN', 'ADMIN_SYSTEM']}><SignupRequestsPage /></RoleRoute>} />
              </Route>
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </QueryClientProvider>
    </KeycloakProvider>
  )
}
