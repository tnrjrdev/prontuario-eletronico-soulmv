import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Stethoscope, Plus, ArrowRightCircle, CheckCircle2, Clock, Activity, MoreVertical, RefreshCw,
} from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { DataTable, type Column } from '../components/DataTable'
import { Modal } from '../components/Modal'
import { AsyncCombobox } from '../components/AsyncCombobox'
import { Button, Badge, Select, type BadgeColor } from '../components/ui'
import { Popover, MenuItem, MenuLabel } from '../components/Popover'
import { useToast } from '../components/Toast'
import { useConfirm } from '../components/ConfirmDialog'
import { atendimentoService, type AtendimentoPayload } from '../services/atendimento.service'
import { pacienteService } from '../services/paciente.service'
import { catalogoService } from '../services/catalogo.service'
import { extractError } from '../services/api'
import { useAuth } from '../hooks/useAuth'
import { STATUS_ATENDIMENTO_LABELS } from '../utils/constants'
import { formatTime } from '../utils/format'
import type { Atendimento, StatusAtendimento } from '../types'

const STATUS_CORES: Record<string, BadgeColor> = {
  AGUARDANDO_TRIAGEM: 'attention',
  EM_TRIAGEM: 'orange',
  AGUARDANDO_ATENDIMENTO: 'attention',
  EM_ATENDIMENTO: 'info',
  INTERNADO: 'info',
  AGUARDANDO_EXAME: 'orange',
  ALTA: 'stable',
  CANCELADO: 'critical',
}

const STATUS_EDITAVEIS: StatusAtendimento[] = [
  'EM_TRIAGEM', 'AGUARDANDO_ATENDIMENTO', 'EM_ATENDIMENTO', 'AGUARDANDO_EXAME', 'CANCELADO',
]

const LIVE_REFETCH = 15_000

export function AtendimentosPage() {
  const { hasRole } = useAuth()
  const navigate = useNavigate()
  const toast = useToast()
  const confirm = useConfirm()
  const qc = useQueryClient()
  const podeCriar = hasRole('RECEPCAO', 'MEDICO', 'ENFERMEIRO')
  const podeMudarStatus = hasRole('MEDICO', 'ENFERMEIRO')
  const podeAlta = hasRole('MEDICO')

  const [status, setStatus] = useState('')
  const [novoAberto, setNovoAberto] = useState(false)

  const filtro = useMemo(() => ({ size: 100, status: (status || undefined) as StatusAtendimento | undefined }), [status])
  const filaQ = useQuery({
    queryKey: ['atendimentos', 'fila', filtro],
    queryFn: () => atendimentoService.listar(filtro),
    refetchInterval: LIVE_REFETCH,
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['atendimentos'] })

  const mudarStatus = useMutation({
    mutationFn: ({ id, novo }: { id: number; novo: string }) => atendimentoService.atualizarStatus(id, novo),
    onSuccess: () => { invalidate(); toast.success('Status atualizado.') },
    onError: (e) => toast.error(extractError(e)),
  })
  const darAlta = useMutation({
    mutationFn: (id: number) => atendimentoService.darAlta(id),
    onSuccess: () => { invalidate(); toast.success('Alta registrada.') },
    onError: (e) => toast.error(extractError(e)),
  })

  const onAlta = async (a: Atendimento) => {
    if (await confirm({ title: 'Confirmar alta', description: `Dar alta clínica a ${a.pacienteNome}?`, variant: 'danger', confirmText: 'Dar alta' })) {
      darAlta.mutate(a.id)
    }
  }

  const columns: Column<Atendimento>[] = [
    { key: 'id', header: 'ID', width: '90px', sortable: true, sortAccessor: (a) => a.id, render: (a) => <span className="font-mono font-bold text-muted-foreground">#{String(a.id).padStart(4, '0')}</span> },
    { key: 'paciente', header: 'Paciente', sortAccessor: (a) => a.pacienteNome, render: (a) => <span className="font-semibold text-foreground">{a.pacienteNome}</span> },
    { key: 'tipo', header: 'Tipo', render: (a) => <span className="text-muted-foreground">{a.tipo}</span> },
    { key: 'setor', header: 'Setor', render: (a) => <span className="text-muted-foreground">{a.setorNome}</span> },
    { key: 'entrada', header: 'Entrada', sortable: true, sortAccessor: (a) => a.dataEntrada, render: (a) => <span className="flex items-center gap-1 text-muted-foreground"><Clock className="h-3 w-3" />{formatTime(a.dataEntrada)}</span> },
    { key: 'status', header: 'Status', render: (a) => <Badge color={STATUS_CORES[a.status] ?? 'slate'}>{STATUS_ATENDIMENTO_LABELS[a.status] ?? a.status}</Badge> },
    {
      key: 'acoes',
      header: '',
      align: 'right',
      width: '150px',
      render: (a) => {
        const final = a.status === 'ALTA' || a.status === 'CANCELADO'
        const ehTriagem = a.status === 'AGUARDANDO_TRIAGEM' || a.status === 'EM_TRIAGEM'
        return (
          <div className="flex items-center justify-end gap-1">
            {ehTriagem ? (
              <Button variant="ghost" size="sm" className="text-orange-600" title="Triagem" onClick={() => navigate(`/triagem/${a.id}`)}><Activity className="h-4 w-4" /></Button>
            ) : !final && (
              <Button variant="ghost" size="sm" className="text-primary" title="Abrir prontuário" onClick={() => navigate(`/pep/${a.id}`)}><ArrowRightCircle className="h-4 w-4" /></Button>
            )}
            {podeAlta && !final && (
              <Button variant="outline" size="sm" className="text-success" onClick={() => onAlta(a)}><CheckCircle2 className="mr-1 h-4 w-4" /> Alta</Button>
            )}
            {podeMudarStatus && !final && (
              <Popover align="end" ariaLabel="Mudar status" trigger={<span className="inline-flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-accent hover:text-foreground"><MoreVertical className="h-4 w-4" /></span>}>
                {({ close }) => (
                  <>
                    <MenuLabel>Mudar status</MenuLabel>
                    {STATUS_EDITAVEIS.filter((s) => s !== a.status).map((s) => (
                      <MenuItem key={s} danger={s === 'CANCELADO'} onClick={() => { close(); mudarStatus.mutate({ id: a.id, novo: s }) }}>
                        {STATUS_ATENDIMENTO_LABELS[s]}
                      </MenuItem>
                    ))}
                  </>
                )}
              </Popover>
            )}
          </div>
        )
      },
    },
  ]

  return (
    <div>
      <PageHeader
        title="Fila de Atendimento"
        subtitle="Fluxo de pacientes na unidade — atualiza automaticamente"
        icon={<Stethoscope />}
        breadcrumbs={[{ label: 'Início', to: '/' }, { label: 'Atendimentos' }]}
        actions={
          <div className="flex items-center gap-2">
            <Button variant="outline" size="icon" title="Atualizar agora" onClick={() => filaQ.refetch()} aria-label="Atualizar">
              <RefreshCw className={filaQ.isFetching ? 'h-4 w-4 animate-spin' : 'h-4 w-4'} />
            </Button>
            {podeCriar && <Button onClick={() => setNovoAberto(true)}><Plus className="mr-2 h-4 w-4" /> Abrir atendimento</Button>}
          </div>
        }
      />

      <DataTable
        data={filaQ.data?.content ?? []}
        columns={columns}
        rowKey={(a) => a.id}
        loading={filaQ.isLoading}
        initialSort={{ key: 'entrada', dir: 'asc' }}
        searchAccessor={(a) => `${a.pacienteNome} ${a.setorNome}`}
        searchPlaceholder="Filtrar fila por paciente ou setor..."
        empty="A fila de atendimento está vazia."
        toolbar={
          <select value={status} onChange={(e) => setStatus(e.target.value)} className="h-9 rounded-lg border border-input bg-background px-3 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
            <option value="">Todos status</option>
            {Object.entries(STATUS_ATENDIMENTO_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        }
      />

      {novoAberto && <NovoAtendimentoModal onClose={() => setNovoAberto(false)} onCriado={() => { invalidate(); setNovoAberto(false) }} />}
    </div>
  )
}

function NovoAtendimentoModal({ onClose, onCriado }: { onClose: () => void; onCriado: () => void }) {
  const toast = useToast()
  const setoresQ = useQuery({ queryKey: ['setores'], queryFn: () => catalogoService.setores() })
  const [form, setForm] = useState<AtendimentoPayload>({ pacienteId: 0, tipo: 'AMBULATORIAL', setorId: 0 })

  const abrir = useMutation({
    mutationFn: () => atendimentoService.abrir({ ...form, pacienteId: Number(form.pacienteId), setorId: Number(form.setorId) }),
    onSuccess: () => { toast.success('Atendimento aberto.'); onCriado() },
    onError: (e) => toast.error(extractError(e)),
  })

  const valido = form.pacienteId > 0 && form.setorId > 0

  return (
    <Modal
      open
      onClose={onClose}
      size="lg"
      title="Abrir atendimento"
      description="Registra a entrada do paciente na fila."
      footer={<><Button variant="outline" onClick={onClose}>Cancelar</Button><Button onClick={() => abrir.mutate()} disabled={!valido || abrir.isPending}>{abrir.isPending ? 'Abrindo…' : 'Confirmar abertura'}</Button></>}
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <AsyncCombobox
            label="Paciente"
            placeholder="Buscar paciente por nome…"
            required
            fetcher={(q) => pacienteService.listar({ nome: q, size: 20 }).then((p) => p.content)}
            getValue={(p) => p.id}
            getLabel={(p) => p.nome}
            getHint={(p) => (p.cpf ? `CPF ${p.cpf}` : undefined)}
            onSelect={(p) => setForm((f) => ({ ...f, pacienteId: p ? p.id : 0 }))}
          />
        </div>
        <Select label="Tipo de atendimento" value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value })}>
          <option value="AMBULATORIAL">Ambulatorial (consulta)</option>
          <option value="EMERGENCIA">Pronto-socorro</option>
          <option value="INTERNACAO">Internação</option>
        </Select>
        <Select label="Setor / especialidade" value={form.setorId || ''} onChange={(e) => setForm({ ...form, setorId: Number(e.target.value) })} required>
          <option value="">Selecione o setor…</option>
          {setoresQ.data?.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
        </Select>
      </div>
    </Modal>
  )
}
