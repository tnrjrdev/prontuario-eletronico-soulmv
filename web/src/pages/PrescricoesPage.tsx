import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Pill, Eye, MoreVertical, PauseCircle, PlayCircle, StopCircle, Stethoscope } from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { DataTable, type Column } from '../components/DataTable'
import { Modal } from '../components/Modal'
import { Button, Badge, type BadgeColor } from '../components/ui'
import { Popover, MenuItem } from '../components/Popover'
import { useToast } from '../components/Toast'
import { prescricaoService } from '../services/prescricao.service'
import { extractError } from '../services/api'
import { useAuth } from '../hooks/useAuth'
import { formatDate } from '../utils/format'
import { VIA_ADMINISTRACAO_LABELS } from '../utils/constants'
import type { Prescricao, StatusPrescricao } from '../types'

const STATUS_LABEL: Record<StatusPrescricao, string> = {
  ATIVA: 'Ativa',
  SUSPENSA: 'Suspensa',
  ENCERRADA: 'Encerrada',
}
const STATUS_COR: Record<StatusPrescricao, BadgeColor> = {
  ATIVA: 'stable',
  SUSPENSA: 'attention',
  ENCERRADA: 'slate',
}

export function PrescricoesPage() {
  const navigate = useNavigate()
  const { hasRole } = useAuth()
  const toast = useToast()
  const qc = useQueryClient()
  const podeAlterar = hasRole('MEDICO')

  const [status, setStatus] = useState('')
  const [verItens, setVerItens] = useState<Prescricao | null>(null)

  const filtro = useMemo(() => ({ size: 100, status: (status || undefined) as StatusPrescricao | undefined }), [status])
  const listaQ = useQuery({ queryKey: ['prescricoes', filtro], queryFn: () => prescricaoService.listar(filtro) })

  const mudarStatus = useMutation({
    mutationFn: ({ id, novo }: { id: number; novo: StatusPrescricao }) => prescricaoService.atualizarStatus(id, novo),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['prescricoes'] }),
  })

  const onStatus = async (p: Prescricao, novo: StatusPrescricao) => {
    try {
      await mudarStatus.mutateAsync({ id: p.id, novo })
      toast.success(`Prescrição #${p.id} agora está ${STATUS_LABEL[novo].toLowerCase()}.`)
    } catch (e) {
      toast.error(extractError(e))
    }
  }

  const columns: Column<Prescricao>[] = [
    { key: 'data', header: 'Data', sortable: true, sortAccessor: (p) => p.dataHora, render: (p) => <span className="font-medium text-foreground">{formatDate(p.dataHora)}</span> },
    { key: 'paciente', header: 'Paciente', sortAccessor: (p) => p.pacienteNome ?? '', render: (p) => <span className="font-semibold text-foreground">{p.pacienteNome ?? '—'}</span> },
    { key: 'medico', header: 'Prescritor', render: (p) => p.medicoNome },
    {
      key: 'itens',
      header: 'Itens',
      render: (p) => (
        <span className="text-muted-foreground">
          {p.itens.length} item(ns)
          {p.itens.some((i) => i.medicamentoControlado) && <Badge color="critical" className="ml-2 text-[10px]">Controlado</Badge>}
        </span>
      ),
    },
    { key: 'status', header: 'Status', render: (p) => <Badge color={STATUS_COR[p.status]}>{STATUS_LABEL[p.status]}</Badge> },
    {
      key: 'acoes',
      header: '',
      align: 'right',
      width: '120px',
      render: (p) => (
        <div className="flex items-center justify-end gap-1">
          <Button variant="ghost" size="sm" title="Ver itens" onClick={() => setVerItens(p)}><Eye className="h-4 w-4" /></Button>
          <Button variant="ghost" size="sm" title="Abrir prontuário" onClick={() => navigate(`/pep/${p.atendimentoId}`)}><Stethoscope className="h-4 w-4" /></Button>
          {podeAlterar && p.status !== 'ENCERRADA' && (
            <Popover
              align="end"
              ariaLabel="Alterar status"
              trigger={<span className="inline-flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-accent hover:text-foreground"><MoreVertical className="h-4 w-4" /></span>}
            >
              {({ close }) => (
                <>
                  {p.status === 'ATIVA' && <MenuItem icon={<PauseCircle />} onClick={() => { close(); onStatus(p, 'SUSPENSA') }}>Suspender</MenuItem>}
                  {p.status === 'SUSPENSA' && <MenuItem icon={<PlayCircle />} onClick={() => { close(); onStatus(p, 'ATIVA') }}>Reativar</MenuItem>}
                  <MenuItem icon={<StopCircle />} danger onClick={() => { close(); onStatus(p, 'ENCERRADA') }}>Encerrar</MenuItem>
                </>
              )}
            </Popover>
          )}
        </div>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Prescrições"
        subtitle="Prescrições médicas de todos os atendimentos"
        icon={<Pill />}
        breadcrumbs={[{ label: 'Início', to: '/' }, { label: 'Prescrições' }]}
      />

      <DataTable
        data={listaQ.data?.content ?? []}
        columns={columns}
        rowKey={(p) => p.id}
        loading={listaQ.isLoading}
        initialSort={{ key: 'data', dir: 'desc' }}
        searchAccessor={(p) => `${p.pacienteNome ?? ''} ${p.medicoNome}`}
        searchPlaceholder="Buscar por paciente ou prescritor..."
        empty="Nenhuma prescrição encontrada."
        toolbar={
          <select
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            className="h-9 rounded-lg border border-input bg-background px-3 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          >
            <option value="">Todos status</option>
            {Object.entries(STATUS_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        }
      />

      {verItens && (
        <Modal
          open
          onClose={() => setVerItens(null)}
          size="lg"
          title={`Prescrição #${verItens.id}`}
          description={`${verItens.pacienteNome ?? ''} • ${verItens.medicoNome} • ${formatDate(verItens.dataHora)}`}
          footer={<Button variant="outline" onClick={() => setVerItens(null)}>Fechar</Button>}
        >
          {verItens.observacao && <p className="mb-4 rounded-lg bg-muted/40 p-3 text-sm text-muted-foreground">{verItens.observacao}</p>}
          <table className="w-full text-sm">
            <thead className="border-b border-border text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              <tr>
                <th className="py-2 pr-3">Medicamento</th>
                <th className="py-2 pr-3">Dose</th>
                <th className="py-2 pr-3">Via</th>
                <th className="py-2 pr-3">Frequência</th>
                <th className="py-2">Duração</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {verItens.itens.map((i) => (
                <tr key={i.id}>
                  <td className="py-2 pr-3 font-medium text-foreground">
                    {i.medicamentoNome}
                    {i.medicamentoControlado && <Badge color="critical" className="ml-2 text-[10px]">Controlado</Badge>}
                  </td>
                  <td className="py-2 pr-3">{i.dose}</td>
                  <td className="py-2 pr-3">{VIA_ADMINISTRACAO_LABELS[i.via]}</td>
                  <td className="py-2 pr-3">{i.frequencia}</td>
                  <td className="py-2 text-muted-foreground">{i.duracao || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Modal>
      )}
    </div>
  )
}
