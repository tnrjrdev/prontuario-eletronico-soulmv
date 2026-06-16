import React, { createContext, useCallback, useContext, useRef, useState } from 'react'
import { AlertTriangle } from 'lucide-react'
import { Modal } from './Modal'
import { Button } from './ui'
import { cn } from '../utils/cn'

interface ConfirmOptions {
  title?: string
  description?: React.ReactNode
  confirmText?: string
  cancelText?: string
  variant?: 'default' | 'danger'
}

type ConfirmFn = (opts: ConfirmOptions) => Promise<boolean>

const ConfirmContext = createContext<ConfirmFn | null>(null)

export function useConfirm(): ConfirmFn {
  const ctx = useContext(ConfirmContext)
  if (!ctx) throw new Error('useConfirm deve ser usado dentro de ConfirmProvider')
  return ctx
}

/**
 * Provê confirm(opts) baseado em Promise — substitui o confirm() nativo.
 * Ex.: if (await confirm({ title: 'Dar alta?', variant: 'danger' })) { ... }
 */
export function ConfirmProvider({ children }: { children: React.ReactNode }) {
  const [opts, setOpts] = useState<ConfirmOptions | null>(null)
  const resolver = useRef<((v: boolean) => void) | null>(null)

  const confirm = useCallback<ConfirmFn>((options) => {
    setOpts(options)
    return new Promise<boolean>((resolve) => {
      resolver.current = resolve
    })
  }, [])

  const close = (result: boolean) => {
    resolver.current?.(result)
    resolver.current = null
    setOpts(null)
  }

  const danger = opts?.variant === 'danger'

  return (
    <ConfirmContext.Provider value={confirm}>
      {children}
      <Modal open={opts !== null} onClose={() => close(false)} size="sm">
        <div className="flex gap-4">
          <div
            className={cn(
              'flex h-11 w-11 shrink-0 items-center justify-center rounded-full',
              danger ? 'bg-destructive/10 text-destructive' : 'bg-primary/10 text-primary'
            )}
          >
            <AlertTriangle className="h-5 w-5" />
          </div>
          <div className="flex-1">
            <h2 className="text-base font-semibold text-foreground">{opts?.title ?? 'Confirmar ação'}</h2>
            {opts?.description && <p className="mt-1 text-sm text-muted-foreground">{opts.description}</p>}
            <div className="mt-5 flex justify-end gap-2">
              <Button variant="outline" size="sm" onClick={() => close(false)}>
                {opts?.cancelText ?? 'Cancelar'}
              </Button>
              <Button variant={danger ? 'danger' : 'primary'} size="sm" onClick={() => close(true)}>
                {opts?.confirmText ?? 'Confirmar'}
              </Button>
            </div>
          </div>
        </div>
      </Modal>
    </ConfirmContext.Provider>
  )
}
