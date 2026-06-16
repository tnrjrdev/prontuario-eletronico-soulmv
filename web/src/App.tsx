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
import { TriagemPage } from './pages/TriagemPage'
import { EnfermagemPage } from './pages/EnfermagemPage'
import { ExamesPage } from './pages/ExamesPage'
import { FaturamentoPage } from './pages/FaturamentoPage'
import { EmBrevePage } from './pages/EmBrevePage'
import { AgendaPage } from './pages/AgendaPage'
import { InternacoesPage } from './pages/InternacoesPage'
import { RelatoriosPage } from './pages/RelatoriosPage'
import { ConfiguracoesPage } from './pages/ConfiguracoesPage'

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
        <Route path="/triagem/:id" element={<TriagemPage />} />
        <Route path="/pep/:id" element={<PepPage />} />
        <Route path="/enfermagem" element={<EnfermagemPage />} />
        <Route path="/exames" element={<ExamesPage />} />
        <Route path="/faturamento" element={<FaturamentoPage />} />

        {/* Módulos com backend */}
        <Route path="/agenda" element={<AgendaPage />} />
        <Route path="/internacoes" element={<InternacoesPage />} />
        <Route path="/relatorios" element={<RelatoriosPage />} />

        <Route
          path="/configuracoes"
          element={
            <RoleRoute roles={['ADMIN']}>
              <ConfiguracoesPage />
            </RoleRoute>
          }
        />

        {/* Módulos do roadmap (placeholder até a tela) */}
        <Route path="/prontuarios" element={<EmBrevePage />} />
        <Route path="/prescricoes" element={<EmBrevePage />} />
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
