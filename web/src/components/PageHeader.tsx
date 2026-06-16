import React from 'react'
import { Link } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
import { cn } from '../utils/cn'

export interface Crumb {
  label: string
  to?: string
}

/**
 * Cabeçalho padrão de página: breadcrumbs + título + subtítulo + ações.
 * Substitui os cabeçalhos duplicados em cada tela.
 */
export function PageHeader({
  title,
  subtitle,
  breadcrumbs,
  actions,
  icon,
  className,
}: {
  title: React.ReactNode
  subtitle?: React.ReactNode
  breadcrumbs?: Crumb[]
  actions?: React.ReactNode
  icon?: React.ReactNode
  className?: string
}) {
  return (
    <div className={cn('mb-6', className)}>
      {breadcrumbs && breadcrumbs.length > 0 && (
        <nav aria-label="breadcrumb" className="mb-2 flex items-center gap-1 text-xs font-medium text-muted-foreground">
          {breadcrumbs.map((c, i) => {
            const last = i === breadcrumbs.length - 1
            return (
              <React.Fragment key={`${c.label}-${i}`}>
                {c.to && !last ? (
                  <Link to={c.to} className="transition-colors hover:text-foreground">{c.label}</Link>
                ) : (
                  <span className={cn(last && 'text-foreground')}>{c.label}</span>
                )}
                {!last && <ChevronRight className="h-3.5 w-3.5 opacity-50" />}
              </React.Fragment>
            )
          })}
        </nav>
      )}
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="flex items-start gap-3">
          {icon && (
            <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary [&>svg]:h-6 [&>svg]:w-6">
              {icon}
            </div>
          )}
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-foreground">{title}</h1>
            {subtitle && <p className="mt-0.5 text-sm text-muted-foreground">{subtitle}</p>}
          </div>
        </div>
        {actions && <div className="flex items-center gap-2">{actions}</div>}
      </div>
    </div>
  )
}
