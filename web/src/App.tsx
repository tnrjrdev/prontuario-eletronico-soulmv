import { lazy, Suspense, type ComponentType } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { PrivateRoute } from './routes/PrivateRoute'
import { RoleRoute } from './routes/RoleRoute'
import { AppLayout } from './layouts/AppLayout'
import { PageLoader } from './components/PageLoader'
import { ErrorBoundary } from './components/ErrorBoundary'

/** Helper para lazy-load de páginas com export nomeado. */
function page<T extends Record<string, ComponentType<unknown>>>(loader: () => Promise<T>, name: keyof T) {
  return lazy(() => loader().then((m) => ({ default: m[name] })))
}

const LoginPage = page(() => import('./pages/LoginPage'), 'LoginPage')
const DashboardPage = page(() => import('./pages/DashboardPage'), 'DashboardPage')
const PacientesPage = page(() => import('./pages/PacientesPage'), 'PacientesPage')
const AtendimentosPage = page(() => import('./pages/AtendimentosPage'), 'AtendimentosPage')
const UsuariosPage = page(() => import('./pages/UsuariosPage'), 'UsuariosPage')
const AuditoriaPage = page(() => import('./pages/AuditoriaPage'), 'AuditoriaPage')
const NotFoundPage = page(() => import('./pages/NotFoundPage'), 'NotFoundPage')
const PepPage = page(() => import('./pages/PepPage'), 'PepPage')
const TriagemPage = page(() => import('./pages/TriagemPage'), 'TriagemPage')
const EnfermagemPage = page(() => import('./pages/EnfermagemPage'), 'EnfermagemPage')
const ExamesPage = page(() => import('./pages/ExamesPage'), 'ExamesPage')
const FaturamentoPage = page(() => import('./pages/FaturamentoPage'), 'FaturamentoPage')
const AgendaPage = page(() => import('./pages/AgendaPage'), 'AgendaPage')
const InternacoesPage = page(() => import('./pages/InternacoesPage'), 'InternacoesPage')
const RelatoriosPage = page(() => import('./pages/RelatoriosPage'), 'RelatoriosPage')
const ConfiguracoesPage = page(() => import('./pages/ConfiguracoesPage'), 'ConfiguracoesPage')
const ProntuariosPage = page(() => import('./pages/ProntuariosPage'), 'ProntuariosPage')
const PrescricoesPage = page(() => import('./pages/PrescricoesPage'), 'PrescricoesPage')

export default function App() {
  return (
    <ErrorBoundary>
      <Suspense fallback={<PageLoader />}>
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
            <Route path="/agenda" element={<AgendaPage />} />
            <Route path="/internacoes" element={<InternacoesPage />} />
            <Route path="/relatorios" element={<RelatoriosPage />} />
            <Route path="/prontuarios" element={<ProntuariosPage />} />
            <Route path="/prescricoes" element={<PrescricoesPage />} />

            <Route
              path="/configuracoes"
              element={
                <RoleRoute roles={['ADMIN']}>
                  <ConfiguracoesPage />
                </RoleRoute>
              }
            />
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
      </Suspense>
    </ErrorBoundary>
  )
}
