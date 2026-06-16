import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import {
  Search, Bell, Moon, Sun, LogOut, ChevronDown, Building2, Check, BellOff,
} from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { ROLE_LABELS } from '../utils/constants'
import { ROUTE_LABELS } from '../lib/navigation'
import { Popover, MenuItem, MenuLabel, MenuSeparator } from '../components/Popover'
import { Avatar } from '../components/ui'
import { getStoredTheme, setTheme, type Theme } from '../lib/theme'

const UNIDADES = ['Unidade Central', 'Pronto Socorro Adulto', 'Ambulatório Norte', 'UTI Geral']

function pageLabel(pathname: string): string {
  if (pathname.startsWith('/pep')) return 'Prontuário'
  if (pathname.startsWith('/triagem')) return 'Triagem'
  const base = '/' + (pathname.split('/')[1] ?? '')
  return ROUTE_LABELS[base === '/' ? '/' : base] ?? 'Início'
}

export function Topbar() {
  const { usuario, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [theme, setThemeState] = useState<Theme>(() => getStoredTheme())
  const [unidade, setUnidade] = useState(UNIDADES[0])

  const isDark = theme === 'dark'
  const toggleTheme = () => {
    const next: Theme = isDark ? 'light' : 'dark'
    setTheme(next)
    setThemeState(next)
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const current = pageLabel(location.pathname)

  return (
    <header className="z-10 flex h-16 shrink-0 items-center gap-3 border-b border-border bg-card/80 px-4 backdrop-blur md:px-6">
      {/* Breadcrumb / título */}
      <div className="hidden min-w-0 items-center gap-1.5 text-sm md:flex">
        <span className="text-muted-foreground">Início</span>
        <span className="text-muted-foreground/50">/</span>
        <span className="truncate font-semibold text-foreground">{current}</span>
      </div>

      {/* Busca global */}
      <div className="relative ml-auto w-full max-w-md md:ml-6">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <input
          type="search"
          placeholder="Buscar paciente, prontuário, CPF..."
          className="h-9 w-full rounded-lg border border-input bg-muted/40 pl-9 pr-12 text-sm shadow-xs transition-colors focus-visible:bg-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
        />
        <kbd className="pointer-events-none absolute right-2.5 top-1/2 hidden -translate-y-1/2 rounded border border-border bg-background px-1.5 py-0.5 text-[10px] font-medium text-muted-foreground sm:block">
          Ctrl K
        </kbd>
      </div>

      <div className="flex items-center gap-1">
        {/* Seletor de unidade */}
        <Popover
          ariaLabel="Selecionar unidade"
          align="end"
          trigger={
            <span className="flex h-9 items-center gap-2 rounded-lg border border-border px-3 text-sm font-medium text-foreground transition-colors hover:bg-accent">
              <Building2 className="h-4 w-4 text-muted-foreground" />
              <span className="hidden max-w-[140px] truncate lg:inline">{unidade}</span>
              <ChevronDown className="h-4 w-4 text-muted-foreground" />
            </span>
          }
        >
          {({ close }) => (
            <>
              <MenuLabel>Unidade atual</MenuLabel>
              {UNIDADES.map((u) => (
                <MenuItem
                  key={u}
                  icon={u === unidade ? <Check /> : <span className="h-4 w-4" />}
                  onClick={() => { setUnidade(u); close() }}
                >
                  {u}
                </MenuItem>
              ))}
            </>
          )}
        </Popover>

        {/* Notificações */}
        <Popover
          ariaLabel="Notificações"
          align="end"
          contentClassName="w-80 p-0"
          trigger={
            <span className="flex h-9 w-9 items-center justify-center rounded-lg text-muted-foreground transition-colors hover:bg-accent hover:text-foreground">
              <Bell className="h-5 w-5" />
            </span>
          }
        >
          <>
            <div className="border-b border-border px-4 py-3 text-sm font-semibold text-foreground">Notificações</div>
            <div className="flex flex-col items-center justify-center gap-2 px-4 py-10 text-center text-muted-foreground">
              <BellOff className="h-7 w-7 opacity-30" />
              <p className="text-sm">Sem novas notificações</p>
            </div>
          </>
        </Popover>

        {/* Tema */}
        <button
          type="button"
          onClick={toggleTheme}
          aria-label={isDark ? 'Tema claro' : 'Tema escuro'}
          className="flex h-9 w-9 items-center justify-center rounded-lg text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
        >
          {isDark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
        </button>

        {/* Perfil */}
        <Popover
          ariaLabel="Menu do usuário"
          align="end"
          contentClassName="w-64"
          trigger={
            <span className="flex items-center gap-2 rounded-lg py-1 pl-1 pr-2 transition-colors hover:bg-accent">
              <Avatar name={usuario?.nomeCompleto} size="sm" />
              <span className="hidden text-left leading-tight md:block">
                <span className="block max-w-[140px] truncate text-sm font-semibold text-foreground">{usuario?.nomeCompleto}</span>
                <span className="block text-xs text-muted-foreground">
                  {usuario?.roles.map((r) => ROLE_LABELS[r]).join(', ')}
                </span>
              </span>
              <ChevronDown className="hidden h-4 w-4 text-muted-foreground md:block" />
            </span>
          }
        >
          {({ close }) => (
            <>
              <div className="flex items-center gap-3 px-3 py-2">
                <Avatar name={usuario?.nomeCompleto} size="md" />
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-foreground">{usuario?.nomeCompleto}</p>
                  <p className="truncate text-xs text-muted-foreground">{usuario?.email}</p>
                </div>
              </div>
              <MenuSeparator />
              <MenuItem
                icon={isDark ? <Sun /> : <Moon />}
                onClick={() => { toggleTheme(); }}
              >
                {isDark ? 'Tema claro' : 'Tema escuro'}
              </MenuItem>
              <MenuSeparator />
              <MenuItem icon={<LogOut />} danger onClick={() => { close(); handleLogout() }}>
                Sair do sistema
              </MenuItem>
            </>
          )}
        </Popover>
      </div>
    </header>
  )
}
