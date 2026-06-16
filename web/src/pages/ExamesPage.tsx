import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { FlaskConical, Eye, Download, MoreVertical, Stethoscope } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { PageHeader } from '../components/PageHeader'
import { DataTable, type Column } from '../components/DataTable'
import { Modal } from '../components/Modal'
import { Button, Badge, type BadgeColor } from '../components/ui'
import { Popover, MenuItem, MenuLabel } from '../components/Popover'
import { useToast } from '../components/Toast'
import { exameService } from '../services/exame.service'
import { extractError } from '../services/api'
import { useAuth } from '../hooks/useAuth'
import { formatDate } from '../utils/format'
import { STATUS_EXAME_LABELS } from '../utils/constants'
import type { SolicitacaoExame, StatusExame } from '../types'

const STATUS_COR: Record<StatusExame, BadgeColor> = {
  SOLICITADO: 'slate',
  COLETADO: 'info',
  EM_ANALISE: 'attention',
  LIBERADO: 'stable',
  CANCELADO: 'critical',
}

// Transições manuais permitidas pelo backend (LIBERADO é via resultado; finais não mudam).
const PROXIMOS: Record<string, StatusExame[]> = {
  SOLICITADO: ['COLETADO', 'CANCELADO'],
  COLETADO: ['EM_ANALISE', 'CANCELADO'],
  EM_ANALISE: ['CANCELADO'],
}

export function ExamesPage() {
  const navigate = useNavigate()
  const { hasRole } = useAuth()
  const toast = useToast()
  const qc = useQueryClient()
  const podeMudar = hasRole('MEDICO', 'ENFERMEIRO')

  const [status, setStatus] = useState('')
  const [verResultado, setVerResultado] = useState<SolicitacaoExame | null>(null)

  const filtro = useMemo(() => ({ size: 100, status: (status || undefined) as StatusExame | undefined }), [status])
  const listaQ = useQuery({ queryKey: ['exames', filtro], queryFn: () => exameService.listar(filtro) })

  const mudarStatus = useMutation({
    mutationFn: ({ id, novo }: { id: number; novo: StatusExame }) => exameService.atualizarStatus(id, novo),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['exames'] }),
  })

  const onStatus = async (e: SolicitacaoExame, novo: StatusExame) => {
    try {
      await mudarStatus.mutateAsync({ id: e.id, novo })
      toast.success(`Exame atualizado para ${STATUS_EXAME_LABELS[novo]}.`)
    } catch (err) {
      toast.error(extractError(err))
    }
  }

  const baixar = async (e: SolicitacaoExame) => {
    try {
      await exameService.baixarLaudo(e.id, `laudo-${e.tipoExame}-${e.id}`)
    } catch (err) {
      toast.error(extractError(err))
    }
  }

  const columns: Column<SolicitacaoExame>[] = [
    { key: 'data', header: 'Solicitado em', sortable: true, sortAccessor: (e) => e.dataSolicitacao, render: (e) => <span className="text-muted-foreground">{formatDate(e.dataSolicitacao)}</span> },
    { key: 'paciente', header: 'Paciente', sortAccessor: (e) => e.pacienteNome ?? '', render: (e) => <span className="font-semibold text-foreground">{e.pacienteNome ?? '—'}</span> },
    { key: 'exame', header: 'Exame', render: (e) => <span className="font-medium text-foreground">{e.tipoExame}</span> },
    { key: 'solicitante', header: 'Solicitante', render: (e) => <span className="text-muted-foreground">{e.medicoSolicitanteNome ?? '—'}</span> },
    { key: 'status', header: 'Status', render: (e) => <Badge color={STATUS_COR[e.status]}>{STATUS_EXAME_LABELS[e.status]}</Badge> },
    {
      key: 'acoes',
      header: '',
      align: 'right',
      width: '150px',
      render: (e) => {
        const proximos = PROXIMOS[e.status] ?? []
        return (
          <div className="flex items-center justify-end gap-1">
            {(e.resultado?.resultadoTexto || e.status === 'LIBERADO') && (
              <Button variant="ghost" size="sm" title="Ver resultado" onClick={() => setVerResultado(e)}><Eye className="h-4 w-4" /></Button>
            )}
            {e.resultado?.temLaudo && (
              <Button variant="ghost" size="sm" title="Baixar laudo" onClick={() => baixar(e)}><Download className="h-4 w-4" /></Button>
            )}
            <Button variant="ghost" size="sm" title="Abrir prontuário" onClick={() => navigate(`/pep/${e.atendimentoId}`)}><Stethoscope className="h-4 w-4" /></Button>
            {podeMudar && proximos.length > 0 && (
              <Popover
                align="end"
                ariaLabel="Alterar status"
                trigger={<span className="inline-flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-accent hover:text-foreground"><MoreVertical className="h-4 w-4" /></span>}
              >
                {({ close }) => (
                  <>
                    <MenuLabel>Avançar status</MenuLabel>
                    {proximos.map((s) => (
                      <MenuItem key={s} danger={s === 'CANCELADO'} onClick={() => { close(); onStatus(e, s) }}>{STATUS_EXAME_LABELS[s]}</MenuItem>
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
        title="Exames"
        subtitle="Central de exames solicitados e laudos"
        icon={<FlaskConical />}
        breadcrumbs={[{ label: 'Início', to: '/' }, { label: 'Exames' }]}
      />

      <DataTable
        data={listaQ.data?.content ?? []}
        columns={columns}
        rowKey={(e) => e.id}
        loading={listaQ.isLoading}
        initialSort={{ key: 'data', dir: 'desc' }}
        searchAccessor={(e) => `${e.pacienteNome ?? ''} ${e.tipoExame}`}
        searchPlaceholder="Buscar por paciente ou exame..."
        empty="Nenhum exame encontrado."
        toolbar={
          <select value={status} onChange={(e) => setStatus(e.target.value)} className="h-9 rounded-lg border border-input bg-background px-3 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
            <option value="">Todos status</option>
            {Object.entries(STATUS_EXAME_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        }
      />

      {verResultado && (
        <Modal
          open
          onClose={() => setVerResultado(null)}
          size="lg"
          title={verResultado.tipoExame}
          description={`${verResultado.pacienteNome ?? ''} • Solicitado em ${formatDate(verResultado.dataSolicitacao)}`}
          footer={
            <>
              {verResultado.resultado?.temLaudo && <Button variant="outline" onClick={() => baixar(verResultado)}><Download className="mr-2 h-4 w-4" /> Baixar laudo</Button>}
              <Button variant="outline" onClick={() => setVerResultado(null)}>Fechar</Button>
            </>
          }
        >
          {verResultado.resultado?.resultadoTexto ? (
            <p className="whitespace-pre-wrap text-sm leading-relaxed text-foreground">{verResultado.resultado.resultadoTexto}</p>
          ) : (
            <p className="text-sm text-muted-foreground">Sem resultado em texto.{verResultado.resultado?.temLaudo ? ' Há um laudo em arquivo para download.' : ''}</p>
          )}
          {verResultado.resultado?.liberadoPorNome && (
            <p className="mt-4 border-t border-border pt-3 text-xs text-muted-foreground">
              Liberado por {verResultado.resultado.liberadoPorNome}
              {verResultado.resultado.dataLiberacao && ` em ${formatDate(verResultado.resultado.dataLiberacao)}`}
            </p>
          )}
        </Modal>
      )}
    </div>
  )
}
