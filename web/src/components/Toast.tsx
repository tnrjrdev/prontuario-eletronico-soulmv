import React, { createContext, useCallback, useContext, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { CheckCircle2, Info, AlertTriangle, XCircle, X } from 'lucide-react'
import { cn } from '../utils/cn'

type Variant = 'default' | 'success' | 'error' | 'warning' | 'info'

interface ToastItem {
  id: number
  title?: string
  description?: React.ReactNode
  variant: Variant
  duration: number
}

interface ToastOptions {
  title?: string
  description?: React.ReactNode
  duration?: number
}

interface ToastApi {
  toast: (opts: ToastOptions & { variant?: Variant }) => void
  success: (description: React.ReactNode, opts?: ToastOptions) => void
  error: (description: React.ReactNode, opts?: ToastOptions) => void
  warning: (description: React.ReactNode, opts?: ToastOptions) => void
  info: (description: React.ReactNode, opts?: ToastOptions) => void
}

const ToastContext = createContext<ToastApi | null>(null)

export function useToast(): ToastApi {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast deve ser usado dentro de ToastProvider')
  return ctx
}

const VARIANT_META: Record<Variant, { icon: React.ReactNode; accent: string }> = {
  default: { icon: <Info className="h-5 w-5" />, accent: 'text-foreground' },
  success: { icon: <CheckCircle2 className="h-5 w-5" />, accent: 'text-success' },
  error: { icon: <XCircle className="h-5 w-5" />, accent: 'text-destructive' },
  warning: { icon: <AlertTriangle className="h-5 w-5" />, accent: 'text-warning' },
  info: { icon: <Info className="h-5 w-5" />, accent: 'text-info' },
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([])
  const idRef = useRef(0)

  const dismiss = useCallback((id: number) => {
    setItems((cur) => cur.filter((t) => t.id !== id))
  }, [])

  const push = useCallback(
    (opts: ToastOptions & { variant?: Variant }) => {
      const id = ++idRef.current
      const duration = opts.duration ?? 4500
      setItems((cur) => [...cur, { id, title: opts.title, description: opts.description, variant: opts.variant ?? 'default', duration }])
      if (duration > 0) window.setTimeout(() => dismiss(id), duration)
    },
    [dismiss]
  )

  const api: ToastApi = {
    toast: push,
    success: (description, opts) => push({ ...opts, description, variant: 'success' }),
    error: (description, opts) => push({ ...opts, description, variant: 'error' }),
    warning: (description, opts) => push({ ...opts, description, variant: 'warning' }),
    info: (description, opts) => push({ ...opts, description, variant: 'info' }),
  }

  return (
    <ToastContext.Provider value={api}>
      {children}
      {createPortal(
        <div className="fixed bottom-4 right-4 z-[120] flex w-full max-w-sm flex-col gap-2">
          {items.map((t) => {
            const meta = VARIANT_META[t.variant]
            return (
              <div
                key={t.id}
                role="status"
                className="flex items-start gap-3 rounded-xl border border-border bg-card p-4 shadow-lg animate-slide-in-right"
              >
                <span className={cn('shrink-0', meta.accent)}>{meta.icon}</span>
                <div className="min-w-0 flex-1">
                  {t.title && <p className="text-sm font-semibold text-foreground">{t.title}</p>}
                  {t.description && <p className="text-sm text-muted-foreground">{t.description}</p>}
                </div>
                <button
                  type="button"
                  onClick={() => dismiss(t.id)}
                  aria-label="Dispensar"
                  className="-mr-1 -mt-1 rounded-md p-1 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            )
          })}
        </div>,
        document.body
      )}
    </ToastContext.Provider>
  )
}
