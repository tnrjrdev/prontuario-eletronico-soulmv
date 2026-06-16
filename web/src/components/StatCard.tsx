import React from 'react'
import { TrendingDown, TrendingUp } from 'lucide-react'
import { cn } from '../utils/cn'
import { Skeleton } from './ui'

type Accent = 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'neutral'

const ACCENT_ICON: Record<Accent, string> = {
  primary: 'bg-primary/10 text-primary',
  success: 'bg-success/10 text-success',
  warning: 'bg-warning/10 text-warning',
  danger: 'bg-destructive/10 text-destructive',
  info: 'bg-info/10 text-info',
  neutral: 'bg-muted text-muted-foreground',
}

/**
 * Cartão de indicador (KPI) para dashboards: rótulo, valor, ícone, tendência
 * opcional e slot para sparkline/gráfico (children).
 */
export function StatCard({
  label,
  value,
  icon,
  accent = 'primary',
  trend,
  hint,
  loading,
  children,
  className,
}: {
  label: React.ReactNode
  value: React.ReactNode
  icon?: React.ReactNode
  accent?: Accent
  trend?: { value: number; label?: string }
  hint?: React.ReactNode
  loading?: boolean
  children?: React.ReactNode
  className?: string
}) {
  const up = trend ? trend.value >= 0 : false
  return (
    <div className={cn('rounded-xl border border-border bg-card p-5 shadow-sm transition-shadow hover:shadow-md', className)}>
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate text-sm font-medium text-muted-foreground">{label}</p>
          {loading ? (
            <Skeleton className="mt-2 h-8 w-24" />
          ) : (
            <p className="mt-1 text-3xl font-bold tracking-tight text-foreground">{value}</p>
          )}
        </div>
        {icon && (
          <div className={cn('flex h-10 w-10 shrink-0 items-center justify-center rounded-lg [&>svg]:h-5 [&>svg]:w-5', ACCENT_ICON[accent])}>
            {icon}
          </div>
        )}
      </div>

      {(trend || hint || children) && (
        <div className="mt-3 flex items-end justify-between gap-2">
          <div className="flex items-center gap-2 text-xs">
            {trend && (
              <span className={cn('inline-flex items-center gap-0.5 font-semibold', up ? 'text-success' : 'text-destructive')}>
                {up ? <TrendingUp className="h-3.5 w-3.5" /> : <TrendingDown className="h-3.5 w-3.5" />}
                {up ? '+' : ''}{trend.value}%
              </span>
            )}
            {hint && <span className="text-muted-foreground">{trend?.label ?? hint}</span>}
          </div>
          {children && <div className="h-10 w-24 shrink-0">{children}</div>}
        </div>
      )}
    </div>
  )
}
