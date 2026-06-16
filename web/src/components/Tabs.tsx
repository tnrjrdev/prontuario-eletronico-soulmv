import React, { useRef } from 'react'
import { cn } from '../utils/cn'

export interface TabItem<K extends string = string> {
  key: K
  label: React.ReactNode
  icon?: React.ComponentType<{ className?: string }>
  badge?: React.ReactNode
}

/**
 * Abas acessíveis (role=tablist) com navegação por seta esquerda/direita.
 * Extrai o padrão usado no PEP para reuso em todas as telas.
 */
export function Tabs<K extends string>({
  tabs,
  value,
  onChange,
  className,
}: {
  tabs: TabItem<K>[]
  value: K
  onChange: (key: K) => void
  className?: string
}) {
  const refs = useRef<Record<string, HTMLButtonElement | null>>({})

  const onKeyDown = (e: React.KeyboardEvent) => {
    const idx = tabs.findIndex((t) => t.key === value)
    if (e.key === 'ArrowRight' || e.key === 'ArrowLeft') {
      e.preventDefault()
      const next = e.key === 'ArrowRight' ? (idx + 1) % tabs.length : (idx - 1 + tabs.length) % tabs.length
      const nextKey = tabs[next].key
      onChange(nextKey)
      refs.current[nextKey]?.focus()
    }
  }

  return (
    <div role="tablist" onKeyDown={onKeyDown} className={cn('flex items-center gap-1 border-b border-border', className)}>
      {tabs.map((t) => {
        const active = t.key === value
        const Icon = t.icon
        return (
          <button
            key={t.key}
            ref={(el) => { refs.current[t.key] = el }}
            role="tab"
            aria-selected={active}
            tabIndex={active ? 0 : -1}
            onClick={() => onChange(t.key)}
            className={cn(
              '-mb-px flex items-center gap-2 border-b-2 px-4 py-2.5 text-sm font-medium transition-colors',
              active
                ? 'border-primary text-primary'
                : 'border-transparent text-muted-foreground hover:border-border hover:text-foreground'
            )}
          >
            {Icon && <Icon className="h-4 w-4" />}
            {t.label}
            {t.badge != null && (
              <span className="ml-0.5 rounded-full bg-muted px-1.5 py-0.5 text-[10px] font-semibold text-muted-foreground">{t.badge}</span>
            )}
          </button>
        )
      })}
    </div>
  )
}
