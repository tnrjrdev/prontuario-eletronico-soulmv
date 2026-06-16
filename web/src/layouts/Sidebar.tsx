import { NavLink } from 'react-router-dom'
import { Activity, PanelLeftClose, PanelLeftOpen } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { NAV_GROUPS, type NavItem } from '../lib/navigation'
import { Tooltip } from '../components/Tooltip'
import { cn } from '../utils/cn'

function NavRow({ item, collapsed }: { item: NavItem; collapsed: boolean }) {
  const Icon = item.icon
  const link = (
    <NavLink
      to={item.to}
      end={item.to === '/'}
      className={({ isActive }) =>
        cn(
          'group relative flex items-center rounded-lg text-sm font-medium transition-colors',
          collapsed ? 'h-10 w-10 justify-center' : 'gap-3 px-3 py-2.5',
          isActive
            ? 'bg-primary/10 text-primary'
            : 'text-sidebar-muted hover:bg-sidebar-accent hover:text-sidebar-foreground'
        )
      }
    >
      {({ isActive }) => (
        <>
          {isActive && !collapsed && (
            <span className="absolute left-0 top-1/2 h-5 w-1 -translate-y-1/2 rounded-r-full bg-primary" />
          )}
          <Icon className="h-5 w-5 shrink-0" />
          {!collapsed && <span className="flex-1 truncate">{item.label}</span>}
          {!collapsed && item.soon && (
            <span className="rounded-full bg-muted px-1.5 py-0.5 text-[9px] font-bold uppercase tracking-wide text-muted-foreground">
              em breve
            </span>
          )}
        </>
      )}
    </NavLink>
  )

  return collapsed ? <Tooltip label={item.label} side="right">{link}</Tooltip> : link
}

export function Sidebar({ collapsed, onToggle }: { collapsed: boolean; onToggle: () => void }) {
  const { hasRole } = useAuth()

  return (
    <aside
      className={cn(
        'z-20 flex shrink-0 flex-col border-r border-sidebar-border bg-sidebar transition-[width] duration-200',
        collapsed ? 'w-[72px]' : 'w-64'
      )}
    >
      {/* Marca */}
      <div className={cn('flex h-16 items-center border-b border-sidebar-border', collapsed ? 'justify-center px-2' : 'px-5')}>
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground">
          <Activity className="h-5 w-5" />
        </div>
        {!collapsed && (
          <div className="ml-3 leading-tight">
            <div className="text-sm font-bold tracking-tight text-sidebar-foreground">SOUL MV</div>
            <div className="text-[11px] text-sidebar-muted">Prontuário Eletrônico</div>
          </div>
        )}
      </div>

      {/* Navegação */}
      <nav className="flex-1 space-y-5 overflow-y-auto px-3 py-4 scrollbar-thin">
        {NAV_GROUPS.map((group) => {
          const visible = group.items.filter((i) => hasRole(...i.roles))
          if (visible.length === 0) return null
          return (
            <div key={group.label} className="space-y-1">
              {collapsed ? (
                <div className="mx-auto my-1 h-px w-6 bg-sidebar-border" />
              ) : (
                <div className="px-3 pb-1 text-[10px] font-bold uppercase tracking-wider text-sidebar-muted/70">{group.label}</div>
              )}
              {visible.map((item) => (
                <NavRow key={item.to} item={item} collapsed={collapsed} />
              ))}
            </div>
          )
        })}
      </nav>

      {/* Recolher */}
      <div className="border-t border-sidebar-border p-3">
        <button
          type="button"
          onClick={onToggle}
          aria-label={collapsed ? 'Expandir menu' : 'Recolher menu'}
          className={cn(
            'flex items-center rounded-lg text-sm font-medium text-sidebar-muted transition-colors hover:bg-sidebar-accent hover:text-sidebar-foreground',
            collapsed ? 'h-10 w-10 justify-center' : 'w-full gap-3 px-3 py-2.5'
          )}
        >
          {collapsed ? <PanelLeftOpen className="h-5 w-5" /> : <PanelLeftClose className="h-5 w-5" />}
          {!collapsed && <span>Recolher menu</span>}
        </button>
      </div>
    </aside>
  )
}
