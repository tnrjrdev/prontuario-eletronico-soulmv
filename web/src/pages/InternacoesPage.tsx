import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { BedDouble, BedSingle, LogOut, ArrowRightCircle, Stethoscope, DoorOpen } from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { DataTable, type Column } from '../components/DataTable'
import { StatCard } from '../components/StatCard'
import { Modal } from '../components/Modal'
import { Button, Badge, Select, Card } from '../components/ui'
import { useToast } from '../components/Toast'
import { useConfirm } from '../components/ConfirmDialog'
import {
  useInternados, useInternacoesAbertas, useLeitos, useAlocarLeito, useDarAlta,
} from '../hooks/useInternacoes'
import { useAuth } from '../hooks/useAuth'
import { extractError } from '../services/api'
import { formatDate } from '../utils/format'
import { STATUS_ATENDIMENTO_LABELS } from '../utils/constants'
import type { Atendimento, Leito } from '../types'

export function InternacoesPage() {
  const navigate = useNavigate()
  const { hasRole } = useAuth()
  const toast = useToast()
  const confirm = useConfirm()
  const podeAlocar = hasRole('MEDICO', 'ENFERMEIRO')
  const podeAlta = hasRole('MEDICO')

  const internadosQ = useInternados()
  const aberturasQ = useInternacoesAbertas()
  const leitosQ = useLeitos()
  const alocar = useAlocarLeito()
  const darAlta = useDarAlta()

  const [alocando, setAlocando] = useState<Atendimento | null>(null)

  const internados = internadosQ.data?.content ?? []
  const aguardandoLeito = (aberturasQ.data?.content ?? []).filter(
    (a) => a.status !== 'INTERNADO' && a.status !== 'ALTA' && a.status !== 'CANCELADO'
  )

  const leitos = leitosQ.data ?? []
  const stats = useMemo(() => {
    const ativos = leitos.filter((l) => l.ativo)
    const livres = ativos.filter((l) => l.status === 'LIVRE').length
    const ocupados = ativos.filter((l) => l.status === 'OCUPADO').length
    const taxa = ativos.length ? Math.round((ocupados / ativos.length) * 100) : 0
    return { ativos: ativos.length, livres, ocupados, taxa }
  }, [leitos])

  const leitosLivres = leitos.filter((l) => l.ativo && l.status === 'LIVRE')

  const onAlta = async (a: Atendimento) => {
    const ok = await confirm({
      title: 'Confirmar alta',
      description: `Dar alta a ${a.pacienteNome} e liberar o leito ${a.leitoIdentificador ?? ''}?`,
      variant: 'danger',
      confirmText: 'Dar alta',
    })
    if (!ok) return
    try {
      await darAlta.mutateAsync(a.id)
      toast.success(`Alta registrada para ${a.pacienteNome}.`)
    } catch (e) {
      toast.error(extractError(e))
    }
  }

  const internadosCols: Column<Atendimento>[] = [
    { key: 'paciente', header: 'Paciente', sortable: true, sortAccessor: (a) => a.pacienteNome, render: (a) => <span className="font-semibold text-foreground">{a.pacienteNome}</span> },
    { key: 'leito', header: 'Leito', render: (a) => <Badge color="info">{a.leitoIdentificador ?? '—'}</Badge> },
    { key: 'setor', header: 'Setor', render: (a) => <span className="text-muted-foreground">{a.setorNome}</span> },
    { key: 'profissional', header: 'Responsável', render: (a) => a.profissionalNome ?? '—' },
    { key: 'entrada', header: 'Entrada', sortable: true, sortAccessor: (a) => a.dataEntrada, render: (a) => <span className="text-muted-foreground">{formatDate(a.dataEntrada)}</span> },
    {
      key: 'acoes',
      header: '',
      align: 'right',
      render: (a) => (
        <div className="flex items-center justify-end gap-1">
          <Button variant="ghost" size="sm" title="Abrir prontuário" onClick={() => navigate(`/pep/${a.id}`)}>
            <Stethoscope className="h-4 w-4" />
          </Button>
          {podeAlta && (
            <Button variant="outline" size="sm" className="text-destructive" onClick={() => onAlta(a)}>
              <LogOut className="mr-1 h-4 w-4" /> Alta
            </Button>
          )}
        </div>
      ),
    },
  ]

  const aguardandoCols: Column<Atendimento>[] = [
    { key: 'paciente', header: 'Paciente', render: (a) => <span className="font-semibold text-foreground">{a.pacienteNome}</span> },
    { key: 'setor', header: 'Setor', render: (a) => <span className="text-muted-foreground">{a.setorNome}</span> },
    { key: 'status', header: 'Status', render: (a) => <Badge color="attention">{STATUS_ATENDIMENTO_LABELS[a.status] ?? a.status}</Badge> },
    { key: 'entrada', header: 'Entrada', render: (a) => <span className="text-muted-foreground">{formatDate(a.dataEntrada)}</span> },
    {
      key: 'acoes',
      header: '',
      align: 'right',
      render: (a) => podeAlocar && (
        <Button variant="outline" size="sm" onClick={() => setAlocando(a)}>
          <ArrowRightCircle className="mr-1 h-4 w-4" /> Alocar leito
        </Button>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Internações"
        subtitle="Pacientes internados, ocupação de leitos e admissões"
        icon={<BedDouble />}
        breadcrumbs={[{ label: 'Início', to: '/' }, { label: 'Internações' }]}
      />

      <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Pacientes internados" value={internados.length} icon={<BedDouble />} accent="info" loading={internadosQ.isLoading} />
        <StatCard label="Leitos livres" value={stats.livres} icon={<BedSingle />} accent="success" loading={leitosQ.isLoading} />
        <StatCard label="Leitos ocupados" value={stats.ocupados} icon={<DoorOpen />} accent="warning" loading={leitosQ.isLoading} />
        <StatCard label="Taxa de ocupação" value={`${stats.taxa}%`} icon={<BedDouble />} accent="primary" hint={`${stats.ativos} leitos ativos`} loading={leitosQ.isLoading} />
      </div>

      {aguardandoLeito.length > 0 && (
        <div className="mb-6">
          <h2 className="mb-2 text-sm font-semibold uppercase tracking-wider text-muted-foreground">Aguardando leito</h2>
          <DataTable
            data={aguardandoLeito}
            columns={aguardandoCols}
            rowKey={(a) => a.id}
            loading={aberturasQ.isLoading}
            empty="Nenhuma internação aguardando leito."
          />
        </div>
      )}

      <h2 className="mb-2 text-sm font-semibold uppercase tracking-wider text-muted-foreground">Pacientes internados</h2>
      <DataTable
        data={internados}
        columns={internadosCols}
        rowKey={(a) => a.id}
        loading={internadosQ.isLoading}
        searchAccessor={(a) => `${a.pacienteNome} ${a.leitoIdentificador ?? ''} ${a.setorNome}`}
        searchPlaceholder="Buscar paciente ou leito..."
        empty="Nenhum paciente internado no momento."
      />

      {alocando && (
        <AlocarLeitoModal
          atendimento={alocando}
          leitosLivres={leitosLivres}
          onClose={() => setAlocando(null)}
          saving={alocar.isPending}
          onSubmit={async (leitoId) => {
            try {
              await alocar.mutateAsync({ atendimentoId: alocando.id, leitoId })
              toast.success(`${alocando.pacienteNome} internado(a).`)
              setAlocando(null)
            } catch (e) {
              toast.error(extractError(e))
            }
          }}
        />
      )}
    </div>
  )
}

function AlocarLeitoModal({
  atendimento,
  leitosLivres,
  onClose,
  onSubmit,
  saving,
}: {
  atendimento: Atendimento
  leitosLivres: Leito[]
  onClose: () => void
  onSubmit: (leitoId: number) => void
  saving: boolean
}) {
  const [leitoId, setLeitoId] = useState('')

  return (
    <Modal
      open
      onClose={onClose}
      size="sm"
      title="Alocar leito"
      description={`Internar ${atendimento.pacienteNome}`}
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancelar</Button>
          <Button onClick={() => leitoId && onSubmit(Number(leitoId))} disabled={saving || !leitoId}>
            {saving ? 'Internando…' : 'Confirmar internação'}
          </Button>
        </>
      }
    >
      {leitosLivres.length === 0 ? (
        <Card className="bg-muted/30">
          <p className="text-sm text-muted-foreground">Não há leitos livres disponíveis no momento.</p>
        </Card>
      ) : (
        <Select label="Leito disponível" value={leitoId} onChange={(e) => setLeitoId(e.target.value)}>
          <option value="">Selecione um leito…</option>
          {leitosLivres.map((l) => <option key={l.id} value={l.id}>{l.identificador} — {l.setorNome}</option>)}
        </Select>
      )}
    </Modal>
  )
}
