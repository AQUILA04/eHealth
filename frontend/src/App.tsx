import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { KeycloakProvider } from '@/auth/KeycloakProvider'
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

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
})

export default function App() {
  return (
    <KeycloakProvider>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Routes>
            <Route element={<AppShell />}>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<Dashboard />} />

              {/* Module I — GAP */}
              <Route path="/gap/patients" element={<PatientsPage />} />
              <Route path="/gap/encounters" element={<EncountersPage />} />
              <Route path="/gap/bed-board" element={<BedBoardPage />} />
              <Route path="/gap/appointments" element={<AppointmentsPage />} />

              {/* Module II — DPI */}
              <Route path="/dpi/encounters" element={<ClinicalEncountersPage />} />
              <Route path="/dpi/encounters/:id" element={<ClinicalEncounterDetailPage />} />
              <Route path="/dpi/vitals" element={<VitalsPage />} />
              <Route path="/dpi/medications" element={<MedicationsPage />} />
              <Route path="/dpi/lab-orders" element={<LabOrdersPage />} />

              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </QueryClientProvider>
    </KeycloakProvider>
  )
}
