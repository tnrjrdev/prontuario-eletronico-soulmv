import { Navigate, Route, Routes } from 'react-router-dom'
import { PrivateRoute } from './routes/PrivateRoute'
import { RoleRoute } from './routes/RoleRoute'
import { AppLayout } from './layouts/AppLayout'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { PacientesPage } from './pages/PacientesPage'
import { AtendimentosPage } from './pages/AtendimentosPage'
import { UsuariosPage } from './pages/UsuariosPage'
import { AuditoriaPage } from './pages/AuditoriaPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { PepPage } from './pages/PepPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        element={
          <PrivateRoute>
            <AppLayout />
          </PrivateRoute>
        }
      >
        <Route path="/" element={<DashboardPage />} />
        <Route path="/pacientes" element={<PacientesPage />} />
        <Route path="/atendimentos" element={<AtendimentosPage />} />
        <Route path="/pep/:id" element={<PepPage />} />
        <Route
          path="/usuarios"
          element={
            <RoleRoute roles={['ADMIN']}>
              <UsuariosPage />
            </RoleRoute>
          }
        />
        <Route
          path="/auditoria"
          element={
            <RoleRoute roles={['ADMIN']}>
              <AuditoriaPage />
            </RoleRoute>
          }
        />
      </Route>

      <Route path="/404" element={<NotFoundPage />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  )
}
