import React, { useMemo, useState } from 'react'
import { ChevronDown, ChevronLeft, ChevronRight, ChevronsUpDown, ChevronUp, Search } from 'lucide-react'
import { cn } from '../utils/cn'
import { Skeleton } from './ui'

export interface Column<T> {
  key: string
  header: React.ReactNode
  /** Conteúdo da célula. Se ausente, usa `sortAccessor`. */
  render?: (row: T) => React.ReactNode
  /** Valor para ordenação (obrigatório p/ coluna sortable). */
  sortAccessor?: (row: T) => string | number
  sortable?: boolean
  width?: string
  align?: 'left' | 'center' | 'right'
  className?: string
  headerClassName?: string
}

interface DataTableProps<T> {
  data: T[]
  columns: Column<T>[]
  rowKey: (row: T) => string | number
  loading?: boolean
  /** Texto pesquisável por linha; habilita a busca instantânea. */
  searchAccessor?: (row: T) => string
  searchPlaceholder?: string
  pageSize?: number
  onRowClick?: (row: T) => void
  empty?: React.ReactNode
  /** Filtros/ações extras renderizados ao lado da busca. */
  toolbar?: React.ReactNode
  initialSort?: { key: string; dir: 'asc' | 'desc' }
  className?: string
}

const ALIGN: Record<NonNullable<Column<unknown>['align']>, string> = {
  left: 'text-left',
  center: 'text-center',
  right: 'text-right',
}

export function DataTable<T>({
  data,
  columns,
  rowKey,
  loading,
  searchAccessor,
  searchPlaceholder = 'Buscar...',
  pageSize = 10,
  onRowClick,
  empty,
  toolbar,
  initialSort,
  className,
}: DataTableProps<T>) {
  const [query, setQuery] = useState('')
  const [sort, setSort] = useState<{ key: string; dir: 'asc' | 'desc' } | null>(initialSort ?? null)
  const [page, setPage] = useState(0)

  const filtered = useMemo(() => {
    if (!searchAccessor || !query.trim()) return data
    const q = query.trim().toLowerCase()
    return data.filter((row) => searchAccessor(row).toLowerCase().includes(q))
  }, [data, query, searchAccessor])

  const sorted = useMemo(() => {
    if (!sort) return filtered
    const col = columns.find((c) => c.key === sort.key)
    if (!col?.sortAccessor) return filtered
    const acc = col.sortAccessor
    const dir = sort.dir === 'asc' ? 1 : -1
    return [...filtered].sort((a, b) => {
      const va = acc(a)
      const vb = acc(b)
      if (va < vb) return -1 * dir
      if (va > vb) return 1 * dir
      return 0
    })
  }, [filtered, sort, columns])

  const totalPages = Math.max(1, Math.ceil(sorted.length / pageSize))
  const safePage = Math.min(page, totalPages - 1)
  const paged = useMemo(
    () => sorted.slice(safePage * pageSize, safePage * pageSize + pageSize),
    [sorted, safePage, pageSize]
  )

  const toggleSort = (key: string) => {
    setPage(0)
    setSort((cur) => {
      if (cur?.key !== key) return { key, dir: 'asc' }
      if (cur.dir === 'asc') return { key, dir: 'desc' }
      return null
    })
  }

  const showToolbar = searchAccessor || toolbar
  const colCount = columns.length

  return (
    <div className={cn('overflow-hidden rounded-xl border border-border bg-card shadow-sm', className)}>
      {showToolbar && (
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border bg-muted/20 p-3">
          {searchAccessor ? (
            <div className="relative w-full max-w-xs">
              <Search className="pointer-events-none absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                value={query}
                onChange={(e) => { setQuery(e.target.value); setPage(0) }}
                placeholder={searchPlaceholder}
                className="h-9 w-full rounded-lg border border-input bg-background pl-9 pr-3 text-sm shadow-xs transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </div>
          ) : <span />}
          {toolbar && <div className="flex items-center gap-2">{toolbar}</div>}
        </div>
      )}

      <div className="overflow-x-auto scrollbar-thin">
        <table className="w-full text-sm">
          <thead className="sticky top-0 z-10 border-b border-border bg-muted/50 backdrop-blur">
            <tr className="text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              {columns.map((c) => {
                const active = sort?.key === c.key
                return (
                  <th
                    key={c.key}
                    style={c.width ? { width: c.width } : undefined}
                    className={cn('px-4 py-3', c.align && ALIGN[c.align], c.headerClassName)}
                  >
                    {c.sortable ? (
                      <button
                        type="button"
                        onClick={() => toggleSort(c.key)}
                        className={cn('inline-flex items-center gap-1 transition-colors hover:text-foreground', active && 'text-foreground')}
                      >
                        {c.header}
                        {active ? (
                          sort!.dir === 'asc' ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />
                        ) : (
                          <ChevronsUpDown className="h-3.5 w-3.5 opacity-40" />
                        )}
                      </button>
                    ) : (
                      c.header
                    )}
                  </th>
                )
              })}
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {loading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <tr key={`sk-${i}`}>
                  {columns.map((c) => (
                    <td key={c.key} className="px-4 py-3"><Skeleton className="h-4 w-full max-w-[140px]" /></td>
                  ))}
                </tr>
              ))
            ) : paged.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="px-4 py-12 text-center text-muted-foreground">
                  {empty ?? 'Nenhum registro encontrado.'}
                </td>
              </tr>
            ) : (
              paged.map((row) => (
                <tr
                  key={rowKey(row)}
                  onClick={onRowClick ? () => onRowClick(row) : undefined}
                  className={cn('transition-colors hover:bg-accent/50', onRowClick && 'cursor-pointer')}
                >
                  {columns.map((c) => (
                    <td key={c.key} className={cn('px-4 py-3 text-foreground', c.align && ALIGN[c.align], c.className)}>
                      {c.render ? c.render(row) : c.sortAccessor ? String(c.sortAccessor(row)) : null}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {!loading && sorted.length > pageSize && (
        <div className="flex items-center justify-between border-t border-border px-4 py-3 text-sm text-muted-foreground">
          <span>
            {safePage * pageSize + 1}–{Math.min((safePage + 1) * pageSize, sorted.length)} de {sorted.length}
          </span>
          <div className="flex items-center gap-1">
            <button
              type="button"
              disabled={safePage === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="inline-flex h-8 w-8 items-center justify-center rounded-lg border border-border transition-colors hover:bg-accent disabled:pointer-events-none disabled:opacity-40"
              aria-label="Página anterior"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <span className="px-2 font-medium text-foreground">{safePage + 1} / {totalPages}</span>
            <button
              type="button"
              disabled={safePage >= totalPages - 1}
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              className="inline-flex h-8 w-8 items-center justify-center rounded-lg border border-border transition-colors hover:bg-accent disabled:pointer-events-none disabled:opacity-40"
              aria-label="Próxima página"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
