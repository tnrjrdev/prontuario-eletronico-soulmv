import React, { useEffect, useRef, useState } from 'react'
import { cn } from '../utils/cn'

type Align = 'start' | 'end' | 'center'

interface PopoverProps {
  trigger: React.ReactNode
  children: React.ReactNode | ((api: { close: () => void }) => React.ReactNode)
  align?: Align
  className?: string
  contentClassName?: string
  ariaLabel?: string
}

const ALIGN_CLASSES: Record<Align, string> = {
  start: 'left-0 origin-top-left',
  end: 'right-0 origin-top-right',
  center: 'left-1/2 -translate-x-1/2 origin-top',
}

/**
 * Popover ancorado, fecha ao clicar fora ou pressionar ESC.
 * Base reutilizada por DropdownMenu e pelos menus da topbar.
 */
export function Popover({ trigger, children, align = 'end', className, contentClassName, ariaLabel }: PopoverProps) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const onClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onClick)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onClick)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  return (
    <div ref={ref} className={cn('relative', className)}>
      <button
        type="button"
        aria-haspopup="menu"
        aria-expanded={open}
        aria-label={ariaLabel}
        onClick={() => setOpen((o) => !o)}
        className="inline-flex outline-none"
      >
        {trigger}
      </button>
      {open && (
        <div
          role="menu"
          aria-label={ariaLabel}
          className={cn(
            'absolute top-full z-50 mt-2 min-w-[12rem] rounded-xl border border-border bg-popover p-1 text-popover-foreground shadow-popover animate-pop-in',
            ALIGN_CLASSES[align],
            contentClassName
          )}
        >
          {typeof children === 'function' ? children({ close: () => setOpen(false) }) : children}
        </div>
      )}
    </div>
  )
}

export function MenuLabel({ children }: { children: React.ReactNode }) {
  return <div className="px-3 py-1.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground">{children}</div>
}

export function MenuSeparator() {
  return <div className="my-1 h-px bg-border" />
}

export function MenuItem({
  children,
  icon,
  onClick,
  danger,
  className,
}: {
  children: React.ReactNode
  icon?: React.ReactNode
  onClick?: () => void
  danger?: boolean
  className?: string
}) {
  return (
    <button
      type="button"
      role="menuitem"
      onClick={onClick}
      className={cn(
        'flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
        danger
          ? 'text-destructive hover:bg-destructive/10'
          : 'text-foreground hover:bg-accent hover:text-accent-foreground',
        className
      )}
    >
      {icon && <span className="[&>svg]:h-4 [&>svg]:w-4 shrink-0">{icon}</span>}
      <span className="flex-1 text-left">{children}</span>
    </button>
  )
}
