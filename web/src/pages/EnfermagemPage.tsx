import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Syringe, Search, Pill, CheckSquare, XSquare, MinusSquare, History, AlertTriangle, ShieldCheck, Activity,
} from 'lucide-react'
import { Badge, Button, Spinner, EmptyState, type BadgeColor } from '../components/ui'
import { Modal } from '../components/Modal'
import { useToast } from '../components/Toast'
import { atendimentoService } from '../services/atendimento.service'
import { clinicoService } from '../services/clinico.service'
import { administracaoService } from '../services/administracao.service'
import { extractError } from '../services/api'
import { useAuth } from '../hooks/useAuth'
import { formatDate, formatTime } from '../utils/format'
import { VIA_ADMINISTRACAO_LABELS, STATUS_ADMINISTRACAO_LABELS } from '../utils/constants'
import type { Administracao, ItemPrescricao, StatusAdministracao } from '../types'

const EM_CUIDADO = ['INTERNADO', 'EM_ATENDIMENTO', 'AGUARDANDO_EXAME']

const ADM_COR: Record<StatusAdministracao, BadgeColor> = {
  ADMINISTRADO: 'stable',
  RECUSADO: 'critical',
  NAO_ADMINISTRADO: 'attention',
}

export function EnfermagemPage() {
  const { hasRole } = useAuth()
  const podeChecar = hasRole('ENFERMEIRO')

  const [busca, setBusca] = useState('')
  const [selecionado, setSelecionado] = useState<number | null>(null)

  const atendQ = useQuery({
    queryKey: ['atendimentos', 'enfermagem'],
    queryFn: () => atendimentoService.listar({ size: 200 }),
    refetchInterval: 20_000,
  })
  const pacientes = (atendQ.data?.content ?? []).filter((a) => EM_CUIDADO.includes(a.status))
  const filtrados = pacientes.filter((a) => a.pacienteNome.toLowerCase().includes(busca.toLowerCase()))
  const atual = pacientes.find((a) => a.id === selecionado) ?? null

  return (
    <div className="-m-8 flex h-[calc(100vh-8rem)] flex-col">
      <div className="z-10 flex shrink-0 items-center justify-between border-b border-border bg-card p-4 shadow-sm">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold tracking-tight text-foreground">
            <Syringe className="h-6 w-6 text-primary" /> Painel de Enfermagem
          </h1>
          <p className="text-sm text-muted-foreground">Checagem beira-leito e administração de medicamentos.</p>
        </div>
      </div>

      <div className="flex flex-1 overflow-hidden bg-background">
        {/* Lista de pacientes */}
        <div className="flex w-96 shrink-0 flex-col border-r border-border bg-card">
          <div className="border-b border-border bg-muted/20 p-3">
            <div className="relative">
              <Search className="pointer-events-none absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
                placeholder="Buscar paciente..."
                className="h-9 w-full rounded-lg border border-input bg-background pl-9 pr-3 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </div>
          </div>
          <div className="flex-1 space-y-2 overflow-y-auto scrollbar-thin p-2">
            {atendQ.isLoading ? (
              <div className="flex justify-center py-8"><Spinner /></div>
            ) : filtrados.length === 0 ? (
              <p className="p-4 text-center text-sm text-muted-foreground">Nenhum paciente em cuidado.</p>
            ) : filtrados.map((a) => (
              <button
                key={a.id}
                onClick={() => setSelecionado(a.id)}
                className={`w-full rounded-lg border p-3 text-left transition-all ${selecionado === a.id ? 'border-primary bg-primary/5 ring-1 ring-primary' : 'border-border bg-background hover:border-primary/50'}`}
              >
                <div className="mb-1 flex items-start justify-between">
                  <span className="truncate pr-2 text-sm font-semibold text-foreground">{a.pacienteNome}</span>
                  <Badge color={a.status === 'INTERNADO' ? 'info' : 'slate'} className="shrink-0 text-[10px]">{a.leitoIdentificador ?? a.setorNome}</Badge>
                </div>
                <div className="text-xs text-muted-foreground">Atend. #{String(a.id).padStart(4, '0')}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Painel do paciente */}
        <div className="flex-1 overflow-y-auto scrollbar-thin bg-background p-6">
          {!atual ? (
            <div className="flex h-full flex-col items-center justify-center text-muted-foreground">
              <Syringe className="mb-4 h-16 w-16 opacity-20" />
              <h2 className="text-xl font-semibold text-foreground">Nenhum paciente selecionado</h2>
              <p>Selecione um paciente para ver a prescrição e checar a medicação.</p>
            </div>
          ) : (
            <PainelPaciente atendimentoId={atual.id} pacienteNome={atual.pacienteNome} setor={atual.setorNome} podeChecar={podeChecar} />
          )}
        </div>
      </div>
    </div>
  )
}

function PainelPaciente({ atendimentoId, pacienteNome, setor, podeChecar }: { atendimentoId: number; pacienteNome: string; setor: string; podeChecar: boolean }) {
  const anamneseQ = useQuery({ queryKey: ['anamnese', atendimentoId], queryFn: () => clinicoService.anamnese(atendimentoId) })
  const prescricoesQ = useQuery({ queryKey: ['prescricoes', atendimentoId], queryFn: () => clinicoService.prescricoes(atendimentoId) })
  const [historicoItem, setHistoricoItem] = useState<ItemPrescricao | null>(null)

  const itensAtivos = (prescricoesQ.data ?? []).filter((p) => p.status === 'ATIVA').flatMap((p) => p.itens)
  const alergias = anamneseQ.data?.alergias?.trim()

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="rounded-xl border border-l-4 border-l-primary bg-primary/5 p-4">
        <div className="flex items-start justify-between gap-3">
          <div>
            <h2 className="text-xl font-bold text-foreground">{pacienteNome}</h2>
            <p className="mt-0.5 text-sm text-muted-foreground">Atend. #{String(atendimentoId).padStart(4, '0')} • {setor}</p>
          </div>
          {anamneseQ.isLoading ? null : alergias ? (
            <span className="flex items-center gap-1 rounded-lg bg-destructive/10 px-2.5 py-1 text-sm font-semibold text-destructive"><AlertTriangle className="h-4 w-4" /> Alergia: {alergias}</span>
          ) : (
            <span className="flex items-center gap-1 text-sm font-medium text-success"><ShieldCheck className="h-4 w-4" /> Sem alergias registradas</span>
          )}
        </div>
      </div>

      <div>
        <h3 className="mb-3 flex items-center gap-2 border-b border-border pb-2 text-lg font-semibold text-foreground">
          <Pill className="h-5 w-5" /> Medicamentos prescritos (ativos)
        </h3>

        {prescricoesQ.isLoading ? (
          <div className="flex justify-center py-8"><Spinner /></div>
        ) : itensAtivos.length === 0 ? (
          <EmptyState icon={<Pill />} title="Sem prescrições ativas" description="Este paciente não possui itens prescritos ativos para checagem." />
        ) : (
          <div className="overflow-hidden rounded-xl border border-border bg-card shadow-sm">
            <table className="w-full text-sm">
              <thead className="border-b border-border bg-muted/50 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                <tr>
                  <th className="px-4 py-3">Medicamento</th>
                  <th className="px-4 py-3">Posologia</th>
                  <th className="px-4 py-3 text-right">Checagem</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {itensAtivos.map((item) => (
                  <ItemRow key={item.id} item={item} atendimentoId={atendimentoId} podeChecar={podeChecar} onHistorico={() => setHistoricoItem(item)} />
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {historicoItem && <HistoricoModal item={historicoItem} onClose={() => setHistoricoItem(null)} />}
    </div>
  )
}

function ItemRow({ item, atendimentoId, podeChecar, onHistorico }: { item: ItemPrescricao; atendimentoId: number; podeChecar: boolean; onHistorico: () => void }) {
  const toast = useToast()
  const qc = useQueryClient()

  const registrar = useMutation({
    mutationFn: (status: StatusAdministracao) => administracaoService.registrar(item.id, status),
    onSuccess: (_d, status) => {
      toast.success(`${item.medicamentoNome}: ${STATUS_ADMINISTRACAO_LABELS[status].toLowerCase()}.`)
      qc.invalidateQueries({ queryKey: ['administracoes', item.id] })
      void atendimentoId
    },
    onError: (e) => toast.error(extractError(e)),
  })

  return (
    <tr className="hover:bg-accent/40">
      <td className="px-4 py-3">
        <div className="font-semibold text-foreground">
          {item.medicamentoNome}
          {item.medicamentoControlado && <Badge color="critical" className="ml-2 text-[10px]">Controlado</Badge>}
        </div>
      </td>
      <td className="px-4 py-3 text-muted-foreground">
        {item.dose} • {VIA_ADMINISTRACAO_LABELS[item.via]} • {item.frequencia}
      </td>
      <td className="px-4 py-3">
        <div className="flex items-center justify-end gap-1">
          <Button variant="ghost" size="sm" title="Histórico de checagem" onClick={onHistorico}><History className="h-4 w-4" /></Button>
          {podeChecar && (
            <>
              <Button variant="outline" size="sm" className="border-amber-200 text-amber-700 hover:bg-amber-50" title="Não administrado" disabled={registrar.isPending} onClick={() => registrar.mutate('NAO_ADMINISTRADO')}><MinusSquare className="h-4 w-4" /></Button>
              <Button variant="outline" size="sm" className="border-rose-200 text-rose-700 hover:bg-rose-50" title="Recusado" disabled={registrar.isPending} onClick={() => registrar.mutate('RECUSADO')}><XSquare className="h-4 w-4" /></Button>
              <Button variant="outline" size="sm" className="border-emerald-200 text-emerald-700 hover:bg-emerald-50" title="Administrado" disabled={registrar.isPending} onClick={() => registrar.mutate('ADMINISTRADO')}><CheckSquare className="mr-1 h-4 w-4" /> Checar</Button>
            </>
          )}
        </div>
      </td>
    </tr>
  )
}

function HistoricoModal({ item, onClose }: { item: ItemPrescricao; onClose: () => void }) {
  const histQ = useQuery({ queryKey: ['administracoes', item.id], queryFn: () => administracaoService.listar(item.id) })

  return (
    <Modal open onClose={onClose} size="md" title="Histórico de checagem" description={item.medicamentoNome} footer={<Button variant="outline" onClick={onClose}>Fechar</Button>}>
      {histQ.isLoading ? (
        <div className="flex justify-center py-6"><Spinner /></div>
      ) : (histQ.data?.length ?? 0) === 0 ? (
        <p className="py-4 text-center text-sm text-muted-foreground">Nenhuma checagem registrada.</p>
      ) : (
        <ul className="space-y-2">
          {histQ.data!.map((a: Administracao) => (
            <li key={a.id} className="flex items-center justify-between rounded-lg border border-border p-2.5 text-sm">
              <div>
                <Badge color={ADM_COR[a.status]}>{STATUS_ADMINISTRACAO_LABELS[a.status]}</Badge>
                <span className="ml-2 text-muted-foreground">{a.enfermeiroNome}</span>
                {a.observacao && <p className="mt-1 text-xs text-muted-foreground">{a.observacao}</p>}
              </div>
              <span className="flex items-center gap-1 text-xs text-muted-foreground"><Activity className="h-3 w-3" /> {formatDate(a.dataHoraAdministracao)} ({formatTime(a.dataHoraAdministracao)})</span>
            </li>
          ))}
        </ul>
      )}
    </Modal>
  )
}
