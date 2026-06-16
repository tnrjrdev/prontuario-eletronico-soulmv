import {
  LayoutDashboard, CalendarDays, Users, Stethoscope, FileText, Pill,
  FlaskConical, BedDouble, Syringe, Receipt, BarChart3, ShieldAlert,
  Activity, Settings,
} from 'lucide-react'
import type { Role } from '../types'

export interface NavItem {
  to: string
  label: string
  icon: React.ComponentType<{ className?: string }>
  roles: Role[]
  /** Módulo ainda sem backend — abre página placeholder. */
  soon?: boolean
}

export interface NavGroup {
  label: string
  items: NavItem[]
}

const ALL: Role[] = ['ADMIN', 'MEDICO', 'ENFERMEIRO', 'RECEPCAO', 'FATURAMENTO']

/**
 * Arquitetura de informação inspirada nos ERPs hospitalares de referência
 * (MV/Tasy/Epic). Itens `soon` apontam para páginas placeholder.
 */
export const NAV_GROUPS: NavGroup[] = [
  {
    label: 'Geral',
    items: [
      { to: '/', label: 'Dashboard', icon: LayoutDashboard, roles: ALL },
      { to: '/agenda', label: 'Agenda', icon: CalendarDays, roles: ['ADMIN', 'MEDICO', 'ENFERMEIRO', 'RECEPCAO'] },
    ],
  },
  {
    label: 'Assistencial',
    items: [
      { to: '/pacientes', label: 'Pacientes', icon: Users, roles: ['RECEPCAO', 'MEDICO', 'ENFERMEIRO'] },
      { to: '/atendimentos', label: 'Atendimentos', icon: Stethoscope, roles: ['RECEPCAO', 'MEDICO', 'ENFERMEIRO'] },
      { to: '/prontuarios', label: 'Prontuários', icon: FileText, roles: ['MEDICO', 'ENFERMEIRO'], soon: true },
      { to: '/prescricoes', label: 'Prescrições', icon: Pill, roles: ['MEDICO', 'ENFERMEIRO'], soon: true },
      { to: '/exames', label: 'Exames', icon: FlaskConical, roles: ['ADMIN', 'MEDICO', 'ENFERMEIRO'] },
      { to: '/internacoes', label: 'Internações', icon: BedDouble, roles: ['ADMIN', 'MEDICO', 'ENFERMEIRO'] },
      { to: '/enfermagem', label: 'Enfermagem', icon: Syringe, roles: ['ADMIN', 'ENFERMEIRO'] },
    ],
  },
  {
    label: 'Gestão',
    items: [
      { to: '/faturamento', label: 'Faturamento', icon: Receipt, roles: ['ADMIN', 'FATURAMENTO'] },
      { to: '/relatorios', label: 'Relatórios', icon: BarChart3, roles: ['ADMIN', 'FATURAMENTO'] },
    ],
  },
  {
    label: 'Administração',
    items: [
      { to: '/usuarios', label: 'Usuários', icon: ShieldAlert, roles: ['ADMIN'] },
      { to: '/auditoria', label: 'Auditoria', icon: Activity, roles: ['ADMIN'] },
      { to: '/configuracoes', label: 'Configurações', icon: Settings, roles: ['ADMIN'] },
    ],
  },
]

/** Mapa rota → rótulo para breadcrumbs/títulos da topbar. */
export const ROUTE_LABELS: Record<string, string> = Object.fromEntries(
  NAV_GROUPS.flatMap((g) => g.items).map((i) => [i.to, i.label])
)
