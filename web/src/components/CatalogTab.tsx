import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus, Pencil, Power, Trash2 } from 'lucide-react'
import { DataTable, type Column } from './DataTable'
import { Modal } from './Modal'
import { Button, Badge, Input, Select } from './ui'
import { useToast } from './Toast'
import { useConfirm } from './ConfirmDialog'
import { extractError } from '../services/api'

export type FormValues = Record<string, string | boolean>

export interface FieldDef {
  name: string
  label: string
  type?: 'text' | 'number' | 'select' | 'checkbox'
  options?: { value: string; label: string }[]
  required?: boolean
  full?: boolean
  placeholder?: string
}

export interface CatalogTabProps<T extends { id: number }> {
  queryKey: string
  itemLabel: string
  fetchAll: () => Promise<T[]>
  baseColumns: Column<T>[]
  searchAccessor: (t: T) => string
  fields: FieldDef[]
  toForm: (t: T) => FormValues
  emptyForm: FormValues
  save: (id: number | undefined, form: FormValues) => Promise<unknown>
  getAtivo?: (t: T) => boolean
  toggleStatus?: (t: T, ativo: boolean) => Promise<unknown>
  remove?: (t: T) => Promise<unknown>
}

export function CatalogTab<T extends { id: number }>({
  queryKey, itemLabel, fetchAll, baseColumns, searchAccessor, fields,
  toForm, emptyForm, save, getAtivo, toggleStatus, remove,
}: CatalogTabProps<T>) {
  const qc = useQueryClient()
  const toast = useToast()
  const confirm = useConfirm()
  const listaQ = useQuery({ queryKey: [queryKey], queryFn: fetchAll })

  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<T | null>(null)
  const [form, setForm] = useState<FormValues>(emptyForm)

  const invalidate = () => qc.invalidateQueries({ queryKey: [queryKey] })

  const saveMut = useMutation({
    mutationFn: (vars: { id?: number; form: FormValues }) => save(vars.id, vars.form),
    onSuccess: invalidate,
  })
  const toggleMut = useMutation({
    mutationFn: (vars: { item: T; ativo: boolean }) => toggleStatus!(vars.item, vars.ativo),
    onSuccess: invalidate,
  })
  const removeMut = useMutation({
    mutationFn: (item: T) => remove!(item),
    onSuccess: invalidate,
  })

  const abrirNovo = () => { setEditando(null); setForm(emptyForm); setModalAberto(true) }
  const abrirEdicao = (item: T) => { setEditando(item); setForm(toForm(item)); setModalAberto(true) }

  const onSalvar = async () => {
    try {
      await saveMut.mutateAsync({ id: editando?.id, form })
      toast.success(editando ? `${itemLabel} atualizado(a).` : `${itemLabel} criado(a).`)
      setModalAberto(false)
    } catch (e) {
      toast.error(extractError(e))
    }
  }

  const onToggle = async (item: T) => {
    const ativo = getAtivo!(item)
    try {
      await toggleMut.mutateAsync({ item, ativo: !ativo })
      toast.success(`${itemLabel} ${ativo ? 'inativado(a)' : 'ativado(a)'}.`)
    } catch (e) {
      toast.error(extractError(e))
    }
  }

  const onRemove = async (item: T) => {
    const ok = await confirm({ title: `Excluir ${itemLabel.toLowerCase()}`, description: 'Esta ação não pode ser desfeita.', variant: 'danger', confirmText: 'Excluir' })
    if (!ok) return
    try {
      await removeMut.mutateAsync(item)
      toast.success(`${itemLabel} excluído(a).`)
    } catch (e) {
      toast.error(extractError(e))
    }
  }

  const columns: Column<T>[] = [...baseColumns]
  if (getAtivo) {
    columns.push({
      key: '__status',
      header: 'Status',
      width: '110px',
      render: (t) => getAtivo(t)
        ? <Badge color="stable">Ativo</Badge>
        : <Badge color="slate">Inativo</Badge>,
    })
  }
  columns.push({
    key: '__acoes',
    header: '',
    align: 'right',
    width: '140px',
    render: (t) => (
      <div className="flex items-center justify-end gap-1">
        <Button variant="ghost" size="sm" title="Editar" onClick={() => abrirEdicao(t)}><Pencil className="h-4 w-4" /></Button>
        {toggleStatus && getAtivo && (
          <Button variant="ghost" size="sm" title={getAtivo(t) ? 'Inativar' : 'Ativar'} onClick={() => onToggle(t)}>
            <Power className={getAtivo(t) ? 'h-4 w-4 text-success' : 'h-4 w-4 text-muted-foreground'} />
          </Button>
        )}
        {remove && (
          <Button variant="ghost" size="sm" title="Excluir" className="text-destructive" onClick={() => onRemove(t)}><Trash2 className="h-4 w-4" /></Button>
        )}
      </div>
    ),
  })

  return (
    <>
      <div className="mb-3 flex justify-end">
        <Button size="sm" onClick={abrirNovo}><Plus className="mr-2 h-4 w-4" /> Novo(a) {itemLabel.toLowerCase()}</Button>
      </div>
      <DataTable
        data={listaQ.data ?? []}
        columns={columns}
        rowKey={(t) => t.id}
        loading={listaQ.isLoading}
        searchAccessor={searchAccessor}
        searchPlaceholder={`Buscar ${itemLabel.toLowerCase()}...`}
        pageSize={12}
        empty={`Nenhum(a) ${itemLabel.toLowerCase()} cadastrado(a).`}
      />

      {modalAberto && (
        <Modal
          open
          onClose={() => setModalAberto(false)}
          size="md"
          title={editando ? `Editar ${itemLabel.toLowerCase()}` : `Novo(a) ${itemLabel.toLowerCase()}`}
          footer={
            <>
              <Button variant="outline" onClick={() => setModalAberto(false)}>Cancelar</Button>
              <Button onClick={onSalvar} disabled={saveMut.isPending}>{saveMut.isPending ? 'Salvando…' : 'Salvar'}</Button>
            </>
          }
        >
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {fields.map((f) => {
              const value = form[f.name]
              const onChange = (v: string | boolean) => setForm((cur) => ({ ...cur, [f.name]: v }))
              const wrap = f.full ? 'sm:col-span-2' : ''
              if (f.type === 'checkbox') {
                return (
                  <label key={f.name} className={`flex items-center gap-2 ${wrap}`}>
                    <input type="checkbox" checked={Boolean(value)} onChange={(e) => onChange(e.target.checked)} className="h-4 w-4 rounded border-input text-primary focus:ring-ring" />
                    <span className="text-sm font-medium">{f.label}</span>
                  </label>
                )
              }
              if (f.type === 'select') {
                return (
                  <div key={f.name} className={wrap}>
                    <Select label={f.label} value={String(value ?? '')} onChange={(e) => onChange(e.target.value)} required={f.required}>
                      <option value="">Selecione…</option>
                      {f.options?.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                    </Select>
                  </div>
                )
              }
              return (
                <div key={f.name} className={wrap}>
                  <Input
                    label={f.label}
                    type={f.type === 'number' ? 'number' : 'text'}
                    value={String(value ?? '')}
                    onChange={(e) => onChange(e.target.value)}
                    required={f.required}
                    placeholder={f.placeholder}
                  />
                </div>
              )
            })}
          </div>
        </Modal>
      )}
    </>
  )
}
