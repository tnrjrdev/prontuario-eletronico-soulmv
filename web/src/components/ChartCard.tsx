import React from 'react'
import { ResponsiveContainer } from 'recharts'
import { cn } from '../utils/cn'

/** Paleta de gráficos alinhada à identidade clínica (uso direto no Recharts). */
export const CHART_COLORS = {
  primary: '#2563eb',
  success: '#10b981',
  warning: '#f59e0b',
  danger: '#ef4444',
  info: '#0ea5e9',
  violet: '#7c3aed',
  slate: '#64748b',
}

export const CHART_SERIES = [
  CHART_COLORS.primary,
  CHART_COLORS.success,
  CHART_COLORS.warning,
  CHART_COLORS.violet,
  CHART_COLORS.info,
  CHART_COLORS.danger,
]

/** Props comuns de eixos/grid para um visual consistente entre gráficos. */
export const chartAxisProps = {
  tick: { fontSize: 12, fill: '#94a3b8' },
  axisLine: false,
  tickLine: false,
} as const

/**
 * Container titulado para gráficos Recharts. O filho deve ser um único
 * componente de gráfico (LineChart, BarChart, etc.) — o ResponsiveContainer
 * cuida do dimensionamento.
 */
export function ChartCard({
  title,
  subtitle,
  actions,
  height = 280,
  children,
  className,
}: {
  title?: React.ReactNode
  subtitle?: React.ReactNode
  actions?: React.ReactNode
  height?: number
  children: React.ReactElement
  className?: string
}) {
  return (
    <div className={cn('rounded-xl border border-border bg-card shadow-sm', className)}>
      {(title || actions) && (
        <div className="flex items-start justify-between gap-3 px-5 pt-5">
          <div>
            {title && <h3 className="text-base font-semibold leading-tight tracking-tight text-foreground">{title}</h3>}
            {subtitle && <p className="mt-0.5 text-sm text-muted-foreground">{subtitle}</p>}
          </div>
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      )}
      <div className="px-2 pb-3 pt-4" style={{ height }}>
        <ResponsiveContainer width="100%" height="100%">
          {children}
        </ResponsiveContainer>
      </div>
    </div>
  )
}
