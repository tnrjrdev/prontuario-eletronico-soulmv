import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import type { Role } from '../types'
import { ROLE_LABELS } from '../utils/constants'
import { 
  LayoutDashboard, 
  Users, 
  Stethoscope, 
  ShieldAlert, 
  LogOut, 
  Activity,
  Bell,
  Search
} from 'lucide-react'
import { Button } from '../components/ui'

interface NavItem {
  to: string
  label: string
  icon: React.ReactNode
  roles: Role[]
}

const NAV_ITEMS: NavItem[] = [
  { to: '/', label: 'Dashboard', icon: <LayoutDashboard className="w-5 h-5" />, roles: ['ADMIN', 'MEDICO', 'ENFERMEIRO', 'RECEPCAO', 'FATURAMENTO'] },
  { to: '/pacientes', label: 'Pacientes', icon: <Users className="w-5 h-5" />, roles: ['RECEPCAO', 'MEDICO', 'ENFERMEIRO'] },
  { to: '/atendimentos', label: 'Atendimentos', icon: <Stethoscope className="w-5 h-5" />, roles: ['RECEPCAO', 'MEDICO', 'ENFERMEIRO'] },
  { to: '/usuarios', label: 'Usuários', icon: <ShieldAlert className="w-5 h-5" />, roles: ['ADMIN'] },
  { to: '/auditoria', label: 'Auditoria', icon: <Activity className="w-5 h-5" />, roles: ['ADMIN'] },
]

export function AppLayout() {
  const { usuario, logout, hasRole } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const itensVisiveis = NAV_ITEMS.filter((item) => hasRole(...item.roles))

  return (
    <div className="flex h-screen bg-slate-50/50">
      {/* Sidebar */}
      <aside className="w-64 shrink-0 border-r border-border bg-card flex flex-col z-20 shadow-sm">
        <div className="h-16 flex items-center px-6 border-b border-border">
          <Activity className="h-6 w-6 text-primary mr-2" />
          <span className="text-lg font-bold tracking-tight text-foreground">SOUL MV</span>
        </div>
        <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
          {itensVisiveis.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors ${
                  isActive 
                    ? 'bg-primary/10 text-primary' 
                    : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                }`
              }
            >
              {item.icon}
              {item.label}
            </NavLink>
          ))}
        </nav>
        
        {/* User Profile Area */}
        <div className="p-4 border-t border-border bg-card">
          <div className="flex items-center gap-3 p-2 rounded-md hover:bg-accent transition-colors cursor-pointer group">
            <div className="h-10 w-10 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-sm shrink-0">
              {usuario?.nomeCompleto.substring(0, 2).toUpperCase()}
            </div>
            <div className="flex-1 min-w-0">
              <div className="font-semibold text-sm truncate text-foreground">{usuario?.nomeCompleto}</div>
              <div className="text-xs text-muted-foreground truncate">
                {usuario?.roles.map((r) => ROLE_LABELS[r]).join(', ')}
              </div>
            </div>
          </div>
          <Button variant="ghost" className="w-full justify-start text-muted-foreground hover:text-destructive mt-2" onClick={handleLogout}>
            <LogOut className="mr-2 h-4 w-4" />
            Sair do sistema
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Header */}
        <header className="h-16 flex items-center justify-between border-b border-border bg-card px-8 shrink-0 z-10 shadow-sm">
          <div className="flex items-center gap-4 flex-1">
            <div className="relative w-96 hidden md:block">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                type="text"
                placeholder="Busca rápida por paciente (Nome, CPF, Prontuário)..."
                className="h-9 w-full rounded-md border border-input bg-muted/50 pl-9 pr-4 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              />
            </div>
          </div>
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" className="relative text-muted-foreground">
              <Bell className="h-5 w-5" />
              <span className="absolute top-1.5 right-2 h-2 w-2 rounded-full bg-destructive"></span>
            </Button>
          </div>
        </header>
        
        {/* Page Content */}
        <main className="flex-1 overflow-auto p-8 bg-slate-50/50">
          <div className="mx-auto max-w-7xl">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}
