import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Receipt, DollarSign, FileText, FileCheck2, Plus, Eye, Lock, FileCode2, Download,
} from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { DataTable, type Column } from '../components/DataTable'
import { StatCard } from '../components/StatCard'
import { Modal } from '../components/Modal'
import { Button, Badge, Input, Select, Card, EmptyState, type BadgeColor } from '../components/ui'
import { useToast } from '../components/Toast'
import { useConfirm } from '../components/ConfirmDialog'
import { dashboardService } from '../services/dashboard.service'
import { contaService } from '../services/conta.service'
import { atendimentoService } from '../services/atendimento.service'
import { procedimentoService } from '../services/catalogos.service'
import { extractError } from '../services/api'
import { useAuth } from '../hooks/useAuth'
import { formatDate, formatMoney } from '../utils/format'
import { STATUS_CONTA_LABELS } from '../utils/constants'
import type { ContaHospitalar, StatusConta } from '../types'

const STATUS_COR: Record<StatusConta, BadgeColor> = {
  ABERTA: 'info',
  FECHADA: 'attention',
  FATURADA: 'stable',
  GLOSADA: 'critical',
  CANCELADA: 'slate',
}

export function FaturamentoPage() {
  const { hasRole } = useAuth()
  const podeFaturar = hasRole('FATURAMENTO')

  const [status, setStatus] = useState('')
  const [detalheId, setDetalheId] = useState<number | null>(null)
  const [abrindo, setAbrindo] = useState(false)

  const dashQ = useQuery({ queryKey: ['dashboard', 'faturamento'], queryFn: () => dashboardService.faturamento(), enabled: hasRole('ADMIN', 'FATURAMENTO') })
  const filtro = useMemo(() => ({ size: 100, status: (status || undefined) as StatusConta | undefined }), [status])
  const contasQ = useQuery({ queryKey: ['contas', filtro], queryFn: () => contaService.listar(filtro), enabled: podeFaturar })

  const columns: Column<ContaHospitalar>[] = [
    { key: 'id', header: 'Conta', sortable: true, sortAccessor: (c) => c.id, render: (c) => <span className="font-mono font-semibold text-foreground">#{String(c.id).padStart(5, '0')}</span> },
    { key: 'paciente', header: 'Paciente', sortAccessor: (c) => c.pacienteNome, render: (c) => <span className="font-semibold text-foreground">{c.pacienteNome}</span> },
    { key: 'convenio', header: 'Convênio', render: (c) => <span className="text-muted-foreground">{c.convenioNome ?? 'Particular'}</span> },
    { key: 'valor', header: 'Valor', align: 'right', sortable: true, sortAccessor: (c) => c.valorTotal, render: (c) => <span className="font-medium text-foreground">{formatMoney(c.valorTotal)}</span> },
    { key: 'status', header: 'Status', render: (c) => <Badge color={STATUS_COR[c.status]}>{STATUS_CONTA_LABELS[c.status]}</Badge> },
    { key: 'acoes', header: '', align: 'right', width: '80px', render: (c) => <Button variant="ghost" size="sm" onClick={() => setDetalheId(c.id)} title="Detalhes"><Eye className="h-4 w-4" /></Button> },
  ]

  return (
    <div>
      <PageHeader
        title="Faturamento"
        subtitle="Contas hospitalares, itens TUSS e guias TISS"
        icon={<Receipt />}
        breadcrumbs={[{ label: 'Início', to: '/' }, { label: 'Faturamento' }]}
        actions={podeFaturar && <Button onClick={() => setAbrindo(true)}><Plus className="mr-2 h-4 w-4" /> Abrir conta</Button>}
      />

      <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Faturamento total" value={formatMoney(dashQ.data?.valorTotalGeral)} icon={<DollarSign />} accent="success" loading={dashQ.isLoading} />
        <StatCard label="Total de contas" value={dashQ.data?.totalContas ?? 0} icon={<FileText />} accent="primary" loading={dashQ.isLoading} />
        <StatCard label="Contas abertas" value={dashQ.data?.contasPorStatus?.ABERTA ?? 0} icon={<FileText />} accent="info" loading={dashQ.isLoading} />
        <StatCard label="Faturado" value={formatMoney(dashQ.data?.valorPorStatus?.FATURADA)} icon={<FileCheck2 />} accent="warning" loading={dashQ.isLoading} />
      </div>

      {!podeFaturar ? (
        <Card>
          <EmptyState icon={<Receipt />} title="Operação restrita ao Faturamento" description="As contas hospitalares são geridas pelo perfil Faturamento. Os indicadores acima refletem os dados consolidados." />
        </Card>
      ) : (
        <DataTable
          data={contasQ.data?.content ?? []}
          columns={columns}
          rowKey={(c) => c.id}
          loading={contasQ.isLoading}
          initialSort={{ key: 'id', dir: 'desc' }}
          searchAccessor={(c) => `${c.pacienteNome} ${c.convenioNome ?? ''}`}
          searchPlaceholder="Buscar por paciente ou convênio..."
          empty="Nenhuma conta hospitalar encontrada."
          toolbar={
            <select value={status} onChange={(e) => setStatus(e.target.value)} className="h-9 rounded-lg border border-input bg-background px-3 text-sm shadow-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
              <option value="">Todos status</option>
              {Object.entries(STATUS_CONTA_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
          }
        />
      )}

      {detalheId != null && <ContaDetalheModal contaId={detalheId} onClose={() => setDetalheId(null)} />}
      {abrindo && <AbrirContaModal onClose={() => setAbrindo(false)} onAberta={(id) => { setAbrindo(false); setDetalheId(id) }} />}
    </div>
  )
}

function ContaDetalheModal({ contaId, onClose }: { contaId: number; onClose: () => void }) {
  const qc = useQueryClient()
  const toast = useToast()
  const confirm = useConfirm()

  const contaQ = useQuery({ queryKey: ['conta', contaId], queryFn: () => contaService.buscar(contaId) })
  const guiasQ = useQuery({ queryKey: ['conta', contaId, 'guias'], queryFn: () => contaService.listarGuias(contaId) })
  const procedimentosQ = useQuery({ queryKey: ['cfg-procedimentos'], queryFn: () => procedimentoService.listar() })

  const [procedimentoId, setProcedimentoId] = useState('')
  const [quantidade, setQuantidade] = useState('1')

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['conta', contaId] })
    qc.invalidateQueries({ queryKey: ['contas'] })
  }

  const addItem = useMutation({
    mutationFn: () => contaService.adicionarItem(contaId, { procedimentoId: Number(procedimentoId), quantidade: Number(quantidade) }),
    onSuccess: () => { invalidate(); setProcedimentoId(''); setQuantidade('1'); toast.success('Item adicionado.') },
    onError: (e) => toast.error(extractError(e)),
  })
  const fechar = useMutation({
    mutationFn: () => contaService.fechar(contaId),
    onSuccess: () => { invalidate(); toast.success('Conta fechada.') },
    onError: (e) => toast.error(extractError(e)),
  })
  const gerarGuia = useMutation({
    mutationFn: () => contaService.gerarGuia(contaId),
    onSuccess: () => { invalidate(); qc.invalidateQueries({ queryKey: ['conta', contaId, 'guias'] }); toast.success('Guia TISS gerada.') },
    onError: (e) => toast.error(extractError(e)),
  })

  const conta = contaQ.data
  const aberta = conta?.status === 'ABERTA'

  const onFechar = async () => {
    if (await confirm({ title: 'Fechar conta', description: 'Após fechar, não será possível adicionar novos itens.', confirmText: 'Fechar conta' })) fechar.mutate()
  }

  const baixarXml = async (guiaId: number, numero: string) => {
    try {
      const xml = await contaService.baixarXml(guiaId)
      const url = URL.createObjectURL(new Blob([xml], { type: 'application/xml' }))
      const a = document.createElement('a')
      a.href = url; a.download = `guia-tiss-${numero}.xml`; document.body.appendChild(a); a.click(); a.remove()
      URL.revokeObjectURL(url)
    } catch (e) {
      toast.error(extractError(e))
    }
  }

  return (
    <Modal
      open
      onClose={onClose}
      size="xl"
      title={conta ? `Conta #${String(conta.id).padStart(5, '0')}` : 'Conta'}
      description={conta ? `${conta.pacienteNome} • ${conta.convenioNome ?? 'Particular'}` : undefined}
      footer={<Button variant="outline" onClick={onClose}>Fechar</Button>}
    >
      {!conta ? (
        <p className="py-8 text-center text-sm text-muted-foreground">Carregando…</p>
      ) : (
        <div className="space-y-5">
          <div className="flex items-center justify-between rounded-lg bg-muted/30 p-3">
            <Badge color={STATUS_COR[conta.status]}>{STATUS_CONTA_LABELS[conta.status]}</Badge>
            <div className="text-right">
              <div className="text-xs text-muted-foreground">Valor total</div>
              <div className="text-xl font-bold text-foreground">{formatMoney(conta.valorTotal)}</div>
            </div>
          </div>

          <div>
            <h3 className="mb-2 text-sm font-semibold uppercase tracking-wider text-muted-foreground">Itens</h3>
            {conta.itens.length === 0 ? (
              <p className="text-sm text-muted-foreground">Nenhum item lançado.</p>
            ) : (
              <table className="w-full text-sm">
                <thead className="border-b border-border text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  <tr><th className="py-2 pr-3">Código</th><th className="py-2 pr-3">Descrição</th><th className="py-2 pr-3 text-center">Qtd</th><th className="py-2 pr-3 text-right">Unit.</th><th className="py-2 text-right">Total</th></tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {conta.itens.map((i) => (
                    <tr key={i.id}>
                      <td className="py-2 pr-3 font-mono">{i.codigoTuss}</td>
                      <td className="py-2 pr-3">{i.descricao}</td>
                      <td className="py-2 pr-3 text-center">{i.quantidade}</td>
                      <td className="py-2 pr-3 text-right">{formatMoney(i.valorUnitario)}</td>
                      <td className="py-2 text-right font-medium">{formatMoney(i.valorTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {aberta && (
            <div className="rounded-lg border border-border p-3">
              <h4 className="mb-2 text-sm font-semibold text-foreground">Adicionar item (procedimento TUSS)</h4>
              <div className="flex flex-wrap items-end gap-2">
                <div className="min-w-[240px] flex-1">
                  <Select label="Procedimento" value={procedimentoId} onChange={(e) => setProcedimentoId(e.target.value)}>
                    <option value="">Selecione…</option>
                    {procedimentosQ.data?.filter((p) => p.ativo).map((p) => <option key={p.id} value={p.id}>{p.codigoTuss} — {p.descricao}</option>)}
                  </Select>
                </div>
                <div className="w-24"><Input label="Qtd" type="number" min={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} /></div>
                <Button onClick={() => addItem.mutate()} disabled={!procedimentoId || addItem.isPending}><Plus className="mr-1 h-4 w-4" /> Adicionar</Button>
              </div>
            </div>
          )}

          <div className="flex flex-wrap items-center gap-2 border-t border-border pt-4">
            {aberta && <Button variant="outline" onClick={onFechar} disabled={fechar.isPending}><Lock className="mr-2 h-4 w-4" /> Fechar conta</Button>}
            {conta.status === 'FECHADA' && <Button onClick={() => gerarGuia.mutate()} disabled={gerarGuia.isPending}><FileCode2 className="mr-2 h-4 w-4" /> Gerar guia TISS</Button>}
          </div>

          {(guiasQ.data?.length ?? 0) > 0 && (
            <div>
              <h3 className="mb-2 text-sm font-semibold uppercase tracking-wider text-muted-foreground">Guias TISS</h3>
              <ul className="space-y-2">
                {guiasQ.data!.map((g) => (
                  <li key={g.id} className="flex items-center justify-between rounded-lg border border-border p-2.5">
                    <div className="text-sm">
                      <span className="font-mono font-medium text-foreground">{g.numeroGuia}</span>
                      <span className="ml-2 text-xs text-muted-foreground">{formatDate(g.dataGeracao)}</span>
                      <Badge color="info" className="ml-2 text-[10px]">{g.status}</Badge>
                    </div>
                    <Button variant="ghost" size="sm" onClick={() => baixarXml(g.id, g.numeroGuia)}><Download className="mr-1 h-4 w-4" /> XML</Button>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </Modal>
  )
}

function AbrirContaModal({ onClose, onAberta }: { onClose: () => void; onAberta: (id: number) => void }) {
  const toast = useToast()
  const [atendimentoId, setAtendimentoId] = useState('')
  const atendimentosQ = useQuery({ queryKey: ['atendimentos', 'faturamento'], queryFn: () => atendimentoService.listar({ size: 200 }) })

  const abrir = useMutation({
    mutationFn: () => contaService.abrir(Number(atendimentoId)),
    onSuccess: (c) => { toast.success('Conta aberta.'); onAberta(c.id) },
    onError: (e) => toast.error(extractError(e)),
  })

  return (
    <Modal
      open
      onClose={onClose}
      size="md"
      title="Abrir conta hospitalar"
      description="Vincule a conta a um atendimento."
      footer={<><Button variant="outline" onClick={onClose}>Cancelar</Button><Button onClick={() => abrir.mutate()} disabled={!atendimentoId || abrir.isPending}>{abrir.isPending ? 'Abrindo…' : 'Abrir conta'}</Button></>}
    >
      <Select label="Atendimento" value={atendimentoId} onChange={(e) => setAtendimentoId(e.target.value)}>
        <option value="">Selecione o atendimento…</option>
        {atendimentosQ.data?.content.map((a) => <option key={a.id} value={a.id}>#{String(a.id).padStart(4, '0')} — {a.pacienteNome} ({a.setorNome})</option>)}
      </Select>
    </Modal>
  )
}
