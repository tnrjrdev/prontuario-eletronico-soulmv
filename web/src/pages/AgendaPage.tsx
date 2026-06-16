import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  CalendarDays, Plus, MoreVertical, CheckCircle2, XCircle, UserX, LogIn, Pencil, Stethoscope,
} from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { DataTable, type Column } from '../components/DataTable'
import { Modal } from '../components/Modal'
import { Button, Badge, Input, Select, type BadgeColor } from '../components/ui'
import { Popover, MenuItem, MenuSeparator } from '../components/Popover'
import { useToast } from '../components/Toast'
import { useConfirm } from '../components/ConfirmDialog'
import {
  useAgendamentos, useProfissionais, useSalvarAgendamento,
  useAtualizarStatusAgendamento, useCheckinAgendamento,
} from '../hooks/useAgenda'
import { useAuth } from '../hooks/useAuth'
import { pacienteService } from '../services/paciente.service'
import { catalogoService } from '../services/catalogo.service'
import { extractError } from '../services/api'
import { formatDate } from '../utils/format'
import { STATUS_AGENDAMENTO_LABELS, TIPO_AGENDAMENTO_LABELS } from '../utils/constants'
import type { Agendamento } from '../types'
import type { AgendamentoFiltro, AgendamentoPayload } from '../services/agendamento.service'

const STATUS_COR: Record<string, BadgeColor> = {
  AGENDADO: 'blue',
  CONFIRMADO: 'info',
  REALIZADO: 'stable',
  CANCELADO: 'critical',
  FALTOU: 'attention',
}

const TIPOS = ['CONSULTA', 'RETORNO', 'EXAME', 'PROCEDIMENTO'] as const

export function AgendaPage() {
  const navigate = useNavigate()
  const { hasRole } = useAuth()
  const toast = useToast()
  const confirm = useConfirm()
  const podeAgendar = hasRole('RECEPCAO', 'ADMIN')

  const [dia, setDia] = useState('')
  const [profissionalId, setProfissionalId] = useState('')
  const [status, setStatus] = useState('')
  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<Agendamento | null>(null)

  const filtro: AgendamentoFiltro = useMemo(() => {
    const f: AgendamentoFiltro = { size: 100, sort: 'dataHora' }
    if (dia) {
      f.de = `${dia}T00:00:00`
      f.ate = `${dia}T23:59:59`
    }
    if (profissionalId) f.profissionalId = Number(profissionalId)
    if (status) f.status = status as Agendamento['status']
    return f
  }, [dia, profissionalId, status])

  const agendaQ = useAgendamentos(filtro)
  const profissionaisQ = useProfissionais()
  const salvar = useSalvarAgendamento()
  const mudarStatus = useAtualizarStatusAgendamento()
  const checkin = useCheckinAgendamento()

  const abrirNovo = () => { setEditando(null); setModalAberto(true) }
  const abrirEdicao = (a: Agendamento) => { setEditando(a); setModalAberto(true) }

  const onStatus = async (a: Agendamento, novo: Agendamento['status'], label: string) => {
    const ok = await confirm({
      title: `${label} agendamento`,
      description: `Confirmar "${label.toLowerCase()}" para ${a.pacienteNome}?`,
      variant: novo === 'CANCELADO' || novo === 'FALTOU' ? 'danger' : 'default',
      confirmText: label,
    })
    if (!ok) return
    try {
      await mudarStatus.mutateAsync({ id: a.id, status: novo })
      toast.success(`Agendamento de ${a.pacienteNome} atualizado.`)
    } catch (e) {
      toast.error(extractError(e))
    }
  }

  const onCheckin = async (a: Agendamento) => {
    const ok = await confirm({
      title: 'Realizar check-in',
      description: `Gerar atendimento para ${a.pacienteNome} e enviá-lo à fila?`,
      confirmText: 'Check-in',
    })
    if (!ok) return
    try {
      const atualizado = await checkin.mutateAsync(a.id)
      toast.success('Check-in realizado. Atendimento criado na fila.')
      if (atualizado.atendimentoId) navigate('/atendimentos')
    } catch (e) {
      toast.error(extractError(e))
    }
  }

  const columns: Column<Agendamento>[] = [
    {
      key: 'dataHora',
      header: 'Data / Hora',
      sortable: true,
      sortAccessor: (a) => a.dataHora,
      render: (a) => (
        <div className="font-medium text-foreground">
          {formatDate(a.dataHora)}
          <span className="ml-2 text-xs font-normal text-muted-foreground">{a.duracaoMinutos} min</span>
        </div>
      ),
    },
    { key: 'paciente', header: 'Paciente', sortAccessor: (a) => a.pacienteNome, render: (a) => <span className="font-semibold text-foreground">{a.pacienteNome}</span> },
    { key: 'profissional', header: 'Profissional', render: (a) => a.profissionalNome },
    { key: 'setor', header: 'Setor', render: (a) => <span className="text-muted-foreground">{a.setorNome}</span> },
    { key: 'tipo', header: 'Tipo', render: (a) => <Badge color="slate">{TIPO_AGENDAMENTO_LABELS[a.tipo]}</Badge> },
    { key: 'status', header: 'Status', render: (a) => <Badge color={STATUS_COR[a.status] ?? 'slate'}>{STATUS_AGENDAMENTO_LABELS[a.status]}</Badge> },
    {
      key: 'acoes',
      header: '',
      align: 'right',
      width: '60px',
      render: (a) => {
        const final = a.status === 'REALIZADO' || a.status === 'CANCELADO' || a.status === 'FALTOU'
        if (final) {
          return a.atendimentoId ? (
            <Button variant="ghost" size="sm" onClick={() => navigate('/atendimentos')} title="Ver atendimento gerado">
              <Stethoscope className="h-4 w-4" />
            </Button>
          ) : <span className="text-muted-foreground">—</span>
        }
        return (
          <Popover
            align="end"
            ariaLabel="Ações do agendamento"
            trigger={<span className="inline-flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-accent hover:text-foreground"><MoreVertical className="h-4 w-4" /></span>}
          >
            {({ close }) => (
              <>
                {a.status === 'AGENDADO' && (
                  <MenuItem icon={<CheckCircle2 />} onClick={() => { close(); onStatus(a, 'CONFIRMADO', 'Confirmar') }}>Confirmar presença</MenuItem>
                )}
                {podeAgendar && (
                  <MenuItem icon={<LogIn />} onClick={() => { close(); onCheckin(a) }}>Check-in (gerar atendimento)</MenuItem>
                )}
                {podeAgendar && (
                  <MenuItem icon={<Pencil />} onClick={() => { close(); abrirEdicao(a) }}>Reagendar / editar</MenuItem>
                )}
                <MenuSeparator />
                <MenuItem icon={<UserX />} onClick={() => { close(); onStatus(a, 'FALTOU', 'Faltou') }}>Marcar falta</MenuItem>
                <MenuItem icon={<XCircle />} danger onClick={() => { close(); onStatus(a, 'CANCELADO', 'Cancelar') }}>Cancelar</MenuItem>
              </>
            )}
          </Popover>
        )
      },
    },
  ]

  return (
    <div>
      <PageHeader
        title="Agenda"
        subtitle="Marcação de consultas, exames e procedimentos"
        icon={<CalendarDays />}
        breadcrumbs={[{ label: 'Início', to: '/' }, { label: 'Agenda' }]}
        actions={podeAgendar && <Button onClick={abrirNovo}><Plus className="mr-2 h-4 w-4" /> Nova marcação</Button>}
      />

      <DataTable
        data={agendaQ.data?.content ?? []}
        columns={columns}
        rowKey={(a) => a.id}
        loading={agendaQ.isLoading}
        initialSort={{ key: 'dataHora', dir: 'asc' }}
        searchAccessor={(a) => `${a.pacienteNome} ${a.profissionalNome} ${a.setorNome}`}
        searchPlaceholder="Buscar por paciente ou profissional..."
        empty="Nenhum agendamento para os filtros selecionados."
        toolbar={
          <div className="flex flex-wrap items-center gap-2">
            <input
              type="date"
              value={dia}
              onChange={(e) => setDia(e.target.value)}
              className="h-9 rounded-lg border border-input bg-background px-3 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
            <select
              value={profissionalId}
              onChange={(e) => setProfissionalId(e.target.value)}
              className="h-9 rounded-lg border border-input bg-background px-3 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <option value="">Todos profissionais</option>
              {profissionaisQ.data?.map((p) => <option key={p.id} value={p.id}>{p.nome}</option>)}
            </select>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              className="h-9 rounded-lg border border-input bg-background px-3 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <option value="">Todos status</option>
              {Object.entries(STATUS_AGENDAMENTO_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
          </div>
        }
      />

      {modalAberto && (
        <AgendamentoModal
          agendamento={editando}
          onClose={() => setModalAberto(false)}
          onSubmit={async (payload) => {
            try {
              await salvar.mutateAsync({ id: editando?.id, payload })
              toast.success(editando ? 'Agendamento atualizado.' : 'Agendamento criado.')
              setModalAberto(false)
            } catch (e) {
              toast.error(extractError(e))
            }
          }}
          saving={salvar.isPending}
        />
      )}
    </div>
  )
}

function AgendamentoModal({
  agendamento,
  onClose,
  onSubmit,
  saving,
}: {
  agendamento: Agendamento | null
  onClose: () => void
  onSubmit: (payload: AgendamentoPayload) => void
  saving: boolean
}) {
  const profissionaisQ = useProfissionais()
  const pacientesQ = useQuery({ queryKey: ['pacientes', 'select'], queryFn: () => pacienteService.listar({ size: 200 }) })
  const setoresQ = useQuery({ queryKey: ['setores'], queryFn: () => catalogoService.setores() })
  const conveniosQ = useQuery({ queryKey: ['convenios'], queryFn: () => catalogoService.convenios() })

  const [form, setForm] = useState({
    pacienteId: agendamento?.pacienteId ? String(agendamento.pacienteId) : '',
    profissionalId: agendamento?.profissionalId ? String(agendamento.profissionalId) : '',
    setorId: agendamento?.setorId ? String(agendamento.setorId) : '',
    convenioId: agendamento?.convenioId ? String(agendamento.convenioId) : '',
    tipo: agendamento?.tipo ?? 'CONSULTA',
    dataHora: agendamento?.dataHora ? agendamento.dataHora.slice(0, 16) : '',
    duracaoMinutos: agendamento?.duracaoMinutos ?? 30,
    observacoes: agendamento?.observacoes ?? '',
  })

  const set = (k: keyof typeof form, v: string | number) => setForm((f) => ({ ...f, [k]: v }))

  const submit = () => {
    if (!form.pacienteId || !form.profissionalId || !form.setorId || !form.dataHora) return
    onSubmit({
      pacienteId: Number(form.pacienteId),
      profissionalId: Number(form.profissionalId),
      setorId: Number(form.setorId),
      convenioId: form.convenioId ? Number(form.convenioId) : undefined,
      tipo: form.tipo as AgendamentoPayload['tipo'],
      dataHora: `${form.dataHora}:00`,
      duracaoMinutos: Number(form.duracaoMinutos),
      observacoes: form.observacoes || undefined,
    })
  }

  return (
    <Modal
      open
      onClose={onClose}
      size="lg"
      title={agendamento ? 'Reagendar / editar' : 'Nova marcação'}
      description="Reserve um horário na agenda do profissional."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancelar</Button>
          <Button onClick={submit} disabled={saving}>{saving ? 'Salvando…' : 'Salvar'}</Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <Select label="Paciente" value={form.pacienteId} onChange={(e) => set('pacienteId', e.target.value)} required>
            <option value="">Selecione o paciente…</option>
            {pacientesQ.data?.content.map((p) => <option key={p.id} value={p.id}>{p.nome}</option>)}
          </Select>
        </div>
        <Select label="Profissional" value={form.profissionalId} onChange={(e) => set('profissionalId', e.target.value)} required>
          <option value="">Selecione…</option>
          {profissionaisQ.data?.map((p) => <option key={p.id} value={p.id}>{p.nome}</option>)}
        </Select>
        <Select label="Setor" value={form.setorId} onChange={(e) => set('setorId', e.target.value)} required>
          <option value="">Selecione…</option>
          {setoresQ.data?.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
        </Select>
        <Select label="Tipo" value={form.tipo} onChange={(e) => set('tipo', e.target.value)}>
          {TIPOS.map((t) => <option key={t} value={t}>{TIPO_AGENDAMENTO_LABELS[t]}</option>)}
        </Select>
        <Select label="Convênio (opcional)" value={form.convenioId} onChange={(e) => set('convenioId', e.target.value)}>
          <option value="">Particular / não informado</option>
          {conveniosQ.data?.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
        </Select>
        <Input label="Data e hora" type="datetime-local" value={form.dataHora} onChange={(e) => set('dataHora', e.target.value)} required />
        <Input label="Duração (min)" type="number" min={5} max={480} step={5} value={form.duracaoMinutos} onChange={(e) => set('duracaoMinutos', e.target.value)} />
        <div className="sm:col-span-2">
          <label className="mb-1.5 block text-sm font-medium">Observações</label>
          <textarea
            value={form.observacoes}
            onChange={(e) => set('observacoes', e.target.value)}
            rows={3}
            className="w-full rounded-md border border-input bg-background p-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
            placeholder="Observações da marcação (motivo, preparo, etc.)"
          />
        </div>
      </div>
    </Modal>
  )
}
