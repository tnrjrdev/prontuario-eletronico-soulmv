import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { FileText, FolderOpen, Stethoscope, ArrowRightCircle } from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { DataTable, type Column } from '../components/DataTable'
import { Modal } from '../components/Modal'
import { Button, Badge, Spinner, EmptyState, type BadgeColor } from '../components/ui'
import { pacienteService } from '../services/paciente.service'
import { atendimentoService } from '../services/atendimento.service'
import { calcularIdade, formatCpf, formatDate } from '../utils/format'
import { SEXO_LABELS, STATUS_ATENDIMENTO_LABELS } from '../utils/constants'
import type { Paciente, StatusAtendimento } from '../types'

const STATUS_COR: Record<string, BadgeColor> = {
  AGUARDANDO_TRIAGEM: 'attention',
  EM_TRIAGEM: 'orange',
  AGUARDANDO_ATENDIMENTO: 'attention',
  EM_ATENDIMENTO: 'info',
  INTERNADO: 'info',
  AGUARDANDO_EXAME: 'orange',
  ALTA: 'stable',
  CANCELADO: 'critical',
}

export function ProntuariosPage() {
  const [selecionado, setSelecionado] = useState<Paciente | null>(null)

  const pacientesQ = useQuery({ queryKey: ['pacientes', 'prontuarios'], queryFn: () => pacienteService.listar({ size: 200 }) })

  const columns: Column<Paciente>[] = [
    { key: 'nome', header: 'Paciente', sortable: true, sortAccessor: (p) => p.nome, render: (p) => <span className="font-semibold text-foreground">{p.nome}</span> },
    { key: 'cpf', header: 'CPF', render: (p) => <span className="font-mono text-muted-foreground">{p.cpf ? formatCpf(p.cpf) : '—'}</span> },
    { key: 'idade', header: 'Idade', render: (p) => { const i = calcularIdade(p.dataNascimento); return i != null ? `${i} anos` : '—' } },
    { key: 'sexo', header: 'Sexo', render: (p) => <span className="text-muted-foreground">{p.sexo ? SEXO_LABELS[p.sexo] : '—'}</span> },
    { key: 'convenio', header: 'Convênio', render: (p) => p.convenioNome ?? 'Particular' },
    {
      key: 'acoes',
      header: '',
      align: 'right',
      width: '160px',
      render: (p) => (
        <Button variant="outline" size="sm" onClick={() => setSelecionado(p)}>
          <FolderOpen className="mr-1 h-4 w-4" /> Ver prontuário
        </Button>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Prontuários"
        subtitle="Histórico clínico por paciente"
        icon={<FileText />}
        breadcrumbs={[{ label: 'Início', to: '/' }, { label: 'Prontuários' }]}
      />

      <DataTable
        data={pacientesQ.data?.content ?? []}
        columns={columns}
        rowKey={(p) => p.id}
        loading={pacientesQ.isLoading}
        searchAccessor={(p) => `${p.nome} ${p.cpf ?? ''}`}
        searchPlaceholder="Buscar paciente por nome ou CPF..."
        onRowClick={(p) => setSelecionado(p)}
        empty="Nenhum paciente cadastrado."
      />

      {selecionado && <ProntuarioModal paciente={selecionado} onClose={() => setSelecionado(null)} />}
    </div>
  )
}

function ProntuarioModal({ paciente, onClose }: { paciente: Paciente; onClose: () => void }) {
  const navigate = useNavigate()
  const idade = calcularIdade(paciente.dataNascimento)

  const atendimentosQ = useQuery({
    queryKey: ['atendimentos', { pacienteId: paciente.id }],
    queryFn: () => atendimentoService.listar({ pacienteId: paciente.id, size: 100 }),
  })

  const atendimentos = [...(atendimentosQ.data?.content ?? [])].sort((a, b) => b.dataEntrada.localeCompare(a.dataEntrada))

  const abrirPep = (id: number, status: StatusAtendimento) => {
    onClose()
    navigate(status === 'AGUARDANDO_TRIAGEM' || status === 'EM_TRIAGEM' ? `/triagem/${id}` : `/pep/${id}`)
  }

  return (
    <Modal
      open
      onClose={onClose}
      size="lg"
      title={paciente.nome}
      description={`${idade != null ? `${idade} anos` : ''}${paciente.sexo ? ` • ${SEXO_LABELS[paciente.sexo]}` : ''}${paciente.convenioNome ? ` • ${paciente.convenioNome}` : ''} • Prontuário Nº ${String(paciente.id).padStart(6, '0')}`}
      footer={<Button variant="outline" onClick={onClose}>Fechar</Button>}
    >
      <h3 className="mb-3 text-sm font-semibold uppercase tracking-wider text-muted-foreground">Histórico de atendimentos</h3>
      {atendimentosQ.isLoading ? (
        <div className="flex justify-center py-8"><Spinner /></div>
      ) : atendimentos.length === 0 ? (
        <EmptyState icon={<Stethoscope />} title="Sem atendimentos" description="Este paciente ainda não possui atendimentos registrados." />
      ) : (
        <ul className="space-y-2">
          {atendimentos.map((a) => (
            <li key={a.id} className="flex items-center justify-between gap-3 rounded-lg border border-border bg-card p-3">
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <span className="font-semibold text-foreground">#{String(a.id).padStart(4, '0')}</span>
                  <Badge color={STATUS_COR[a.status] ?? 'slate'}>{STATUS_ATENDIMENTO_LABELS[a.status] ?? a.status}</Badge>
                </div>
                <div className="mt-0.5 text-xs text-muted-foreground">
                  {a.tipo} • {a.setorNome} • Entrada {formatDate(a.dataEntrada)}
                  {a.profissionalNome && ` • ${a.profissionalNome}`}
                </div>
              </div>
              <Button variant="ghost" size="sm" onClick={() => abrirPep(a.id, a.status)} title="Abrir prontuário">
                <ArrowRightCircle className="h-5 w-5 text-primary" />
              </Button>
            </li>
          ))}
        </ul>
      )}
    </Modal>
  )
}
