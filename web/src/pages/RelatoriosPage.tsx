import { useQuery } from '@tanstack/react-query'
import {
  Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, Tooltip as RTooltip, XAxis, YAxis,
} from 'recharts'
import { BarChart3, Users, BedDouble, DollarSign, Activity } from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { StatCard } from '../components/StatCard'
import { ChartCard, CHART_COLORS, CHART_SERIES, chartAxisProps } from '../components/ChartCard'
import { Card, EmptyState } from '../components/ui'
import { dashboardService } from '../services/dashboard.service'
import { useAuth } from '../hooks/useAuth'
import { STATUS_ATENDIMENTO_LABELS, STATUS_LEITO_LABELS } from '../utils/constants'
import { formatMoney } from '../utils/format'

export function RelatoriosPage() {
  const { hasRole } = useAuth()
  const autorizado = hasRole('ADMIN', 'FATURAMENTO')

  const ocupacaoQ = useQuery({ queryKey: ['dashboard', 'ocupacao'], queryFn: () => dashboardService.ocupacao(), enabled: autorizado })
  const atendimentosQ = useQuery({ queryKey: ['dashboard', 'atendimentos'], queryFn: () => dashboardService.atendimentos(), enabled: autorizado })
  const faturamentoQ = useQuery({ queryKey: ['dashboard', 'faturamento'], queryFn: () => dashboardService.faturamento(), enabled: autorizado })

  if (!autorizado) {
    return (
      <div>
        <PageHeader title="Relatórios" subtitle="Indicadores assistenciais e financeiros" icon={<BarChart3 />} />
        <Card>
          <EmptyState icon={<Activity />} title="Acesso restrito" description="Os relatórios gerenciais estão disponíveis para os perfis Administrador e Faturamento." />
        </Card>
      </div>
    )
  }

  const atendData = Object.entries(atendimentosQ.data?.porStatus ?? {}).map(([k, v]) => ({
    nome: STATUS_ATENDIMENTO_LABELS[k] ?? k,
    qtd: v,
  }))
  const leitoData = Object.entries(ocupacaoQ.data?.porStatus ?? {}).map(([k, v]) => ({
    nome: STATUS_LEITO_LABELS[k as keyof typeof STATUS_LEITO_LABELS] ?? k,
    valor: v,
  }))
  const fatData = Object.entries(faturamentoQ.data?.valorPorStatus ?? {}).map(([k, v]) => ({
    nome: k,
    valor: v,
  }))

  return (
    <div>
      <PageHeader
        title="Relatórios"
        subtitle="Indicadores assistenciais e financeiros em tempo real"
        icon={<BarChart3 />}
        breadcrumbs={[{ label: 'Início', to: '/' }, { label: 'Relatórios' }]}
      />

      <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Atendimentos (período)" value={atendimentosQ.data?.total ?? 0} icon={<Users />} accent="primary" loading={atendimentosQ.isLoading} />
        <StatCard label="Taxa de ocupação" value={`${ocupacaoQ.data?.taxaOcupacaoPercent ?? 0}%`} icon={<BedDouble />} accent="info" hint={`${ocupacaoQ.data?.ocupados ?? 0}/${ocupacaoQ.data?.leitosAtivos ?? 0} leitos`} loading={ocupacaoQ.isLoading} />
        <StatCard label="Contas" value={faturamentoQ.data?.totalContas ?? 0} icon={<Activity />} accent="warning" loading={faturamentoQ.isLoading} />
        <StatCard label="Faturamento total" value={formatMoney(faturamentoQ.data?.valorTotalGeral)} icon={<DollarSign />} accent="success" loading={faturamentoQ.isLoading} />
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <ChartCard title="Atendimentos por status" subtitle="Distribuição da operação assistencial">
          <BarChart data={atendData} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
            <XAxis dataKey="nome" {...chartAxisProps} interval={0} angle={-15} textAnchor="end" height={60} />
            <YAxis allowDecimals={false} {...chartAxisProps} />
            <RTooltip cursor={{ fill: 'rgba(37,99,235,0.06)' }} />
            <Bar dataKey="qtd" name="Atendimentos" fill={CHART_COLORS.primary} radius={[6, 6, 0, 0]} />
          </BarChart>
        </ChartCard>

        <ChartCard title="Ocupação de leitos" subtitle="Leitos por situação operacional">
          <PieChart>
            <Pie data={leitoData} dataKey="valor" nameKey="nome" cx="50%" cy="50%" innerRadius={56} outerRadius={88} paddingAngle={2}>
              {leitoData.map((_, i) => <Cell key={i} fill={CHART_SERIES[i % CHART_SERIES.length]} />)}
            </Pie>
            <RTooltip />
            <Legend />
          </PieChart>
        </ChartCard>

        <ChartCard title="Faturamento por status da conta" subtitle="Valor agregado (R$)" className="lg:col-span-2">
          <BarChart data={fatData} margin={{ top: 8, right: 8, left: 8, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
            <XAxis dataKey="nome" {...chartAxisProps} />
            <YAxis {...chartAxisProps} tickFormatter={(v) => formatMoney(v)} width={90} />
            <RTooltip formatter={(v) => formatMoney(Number(v))} cursor={{ fill: 'rgba(16,185,129,0.06)' }} />
            <Bar dataKey="valor" name="Valor" fill={CHART_COLORS.success} radius={[6, 6, 0, 0]} />
          </BarChart>
        </ChartCard>
      </div>
    </div>
  )
}
