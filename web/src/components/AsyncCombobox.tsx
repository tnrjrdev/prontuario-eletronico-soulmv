import { useEffect, useRef, useState } from 'react'
import { Search, X, Loader2 } from 'lucide-react'
import { cn } from '../utils/cn'

interface AsyncComboboxProps<T> {
  label?: string
  placeholder?: string
  /** Rótulo do item já selecionado (para exibir sem refazer a busca). */
  selectedLabel?: string
  fetcher: (query: string) => Promise<T[]>
  getValue: (item: T) => string | number
  getLabel: (item: T) => string
  getHint?: (item: T) => string | undefined
  onSelect: (item: T | null) => void
  minChars?: number
  required?: boolean
  disabled?: boolean
}

/**
 * Seletor com busca assíncrona (typeahead) — substitui <select> que carregaria
 * milhares de registros. Faz debounce e consulta o backend sob demanda.
 */
export function AsyncCombobox<T>({
  label, placeholder = 'Buscar...', selectedLabel, fetcher,
  getValue, getLabel, getHint, onSelect, minChars = 1, required, disabled,
}: AsyncComboboxProps<T>) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<T[]>([])
  const [loading, setLoading] = useState(false)
  const [highlight, setHighlight] = useState(0)
  const [chosen, setChosen] = useState<string | null>(selectedLabel ?? null)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => { setChosen(selectedLabel ?? null) }, [selectedLabel])

  // Fecha ao clicar fora.
  useEffect(() => {
    if (!open) return
    const onClick = (e: MouseEvent) => { if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false) }
    document.addEventListener('mousedown', onClick)
    return () => document.removeEventListener('mousedown', onClick)
  }, [open])

  // Busca com debounce.
  useEffect(() => {
    if (!open) return
    if (query.trim().length < minChars) { setResults([]); return }
    let cancel = false
    setLoading(true)
    const t = setTimeout(() => {
      fetcher(query.trim())
        .then((r) => { if (!cancel) { setResults(r); setHighlight(0) } })
        .catch(() => { if (!cancel) setResults([]) })
        .finally(() => { if (!cancel) setLoading(false) })
    }, 250)
    return () => { cancel = true; clearTimeout(t) }
  }, [query, open, minChars, fetcher])

  const escolher = (item: T) => {
    setChosen(getLabel(item))
    onSelect(item)
    setOpen(false)
    setQuery('')
  }

  const limpar = () => {
    setChosen(null)
    onSelect(null)
    setQuery('')
  }

  const onKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') { e.preventDefault(); setHighlight((h) => Math.min(h + 1, results.length - 1)) }
    else if (e.key === 'ArrowUp') { e.preventDefault(); setHighlight((h) => Math.max(h - 1, 0)) }
    else if (e.key === 'Enter' && results[highlight]) { e.preventDefault(); escolher(results[highlight]) }
    else if (e.key === 'Escape') setOpen(false)
  }

  return (
    <div className="w-full space-y-1.5" ref={ref}>
      {label && <label className="text-sm font-medium leading-none">{label}</label>}
      <div className="relative">
        {chosen && !open ? (
          <div className={cn('flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 text-sm', disabled && 'opacity-50')}>
            <span className="truncate font-medium text-foreground">{chosen}</span>
            {!disabled && (
              <button type="button" onClick={limpar} aria-label="Limpar seleção" className="ml-2 shrink-0 rounded p-0.5 text-muted-foreground hover:bg-accent hover:text-foreground">
                <X className="h-4 w-4" />
              </button>
            )}
          </div>
        ) : (
          <>
            <Search className="pointer-events-none absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <input
              type="text"
              disabled={disabled}
              value={query}
              placeholder={placeholder}
              required={required && !chosen}
              onFocus={() => setOpen(true)}
              onChange={(e) => { setQuery(e.target.value); setOpen(true) }}
              onKeyDown={onKeyDown}
              role="combobox"
              aria-expanded={open}
              aria-autocomplete="list"
              className="flex h-10 w-full rounded-md border border-input bg-background pl-9 pr-8 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
            />
            {loading && <Loader2 className="absolute right-2.5 top-2.5 h-4 w-4 animate-spin text-muted-foreground" />}
          </>
        )}

        {open && (
          <ul role="listbox" className="absolute z-50 mt-1 max-h-64 w-full overflow-auto rounded-lg border border-border bg-popover p-1 shadow-popover">
            {query.trim().length < minChars ? (
              <li className="px-3 py-2 text-sm text-muted-foreground">Digite para buscar…</li>
            ) : loading ? (
              <li className="px-3 py-2 text-sm text-muted-foreground">Buscando…</li>
            ) : results.length === 0 ? (
              <li className="px-3 py-2 text-sm text-muted-foreground">Nenhum resultado.</li>
            ) : (
              results.map((item, i) => {
                const hint = getHint?.(item)
                return (
                  <li key={getValue(item)} role="option" aria-selected={i === highlight}>
                    <button
                      type="button"
                      onMouseEnter={() => setHighlight(i)}
                      onClick={() => escolher(item)}
                      className={cn('flex w-full flex-col items-start rounded-md px-3 py-2 text-left text-sm transition-colors', i === highlight ? 'bg-accent text-accent-foreground' : 'hover:bg-accent')}
                    >
                      <span className="font-medium text-foreground">{getLabel(item)}</span>
                      {hint && <span className="text-xs text-muted-foreground">{hint}</span>}
                    </button>
                  </li>
                )
              })
            )}
          </ul>
        )}
      </div>
    </div>
  )
}
