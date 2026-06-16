import React from 'react'
import { cn } from '../utils/cn'

type Side = 'right' | 'top' | 'bottom' | 'left'

const SIDE_CLASSES: Record<Side, string> = {
  right: 'left-full top-1/2 ml-2 -translate-y-1/2',
  left: 'right-full top-1/2 mr-2 -translate-y-1/2',
  top: 'bottom-full left-1/2 mb-2 -translate-x-1/2',
  bottom: 'top-full left-1/2 mt-2 -translate-x-1/2',
}

/**
 * Tooltip leve baseado em hover/focus (CSS group). Use para ícones e nav recolhida.
 * Não substitui descrições essenciais — apenas reforço visual.
 */
export function Tooltip({
  label,
  side = 'right',
  children,
  className,
}: {
  label: React.ReactNode
  side?: Side
  children: React.ReactNode
  className?: string
}) {
  return (
    <span className={cn('group/tt relative inline-flex', className)}>
      {children}
      <span
        role="tooltip"
        className={cn(
          'pointer-events-none absolute z-[60] whitespace-nowrap rounded-md bg-foreground px-2 py-1 text-xs font-medium text-background opacity-0 shadow-md transition-opacity duration-100 group-hover/tt:opacity-100 group-focus-within/tt:opacity-100',
          SIDE_CLASSES[side]
        )}
      >
        {label}
      </span>
    </span>
  )
}
