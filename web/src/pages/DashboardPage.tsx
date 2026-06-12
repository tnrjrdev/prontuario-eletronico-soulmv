import { useEffect, useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import { Card, Spinner, Badge } from '../components/ui'
import { dashboardService } from '../services/dashboard.service'
import type { AtendimentosDashboard, FaturamentoDashboard, OcupacaoLeitos } from '../types'
import { STATUS_ATENDIMENTO_LABELS } from '../utils/constants'
import { formatMoney } from '../utils/format'
import { Activity, BedDouble, DollarSign, Users } from 'lucide-react'

export function DashboardPage() {
  const { usuario, hasRole } = useAuth()
  const podeVerPaineis = hasRole('ADMIN', 'FATURAMENTO')

  const [ocupacao, setOcupacao] = useState<OcupacaoLeitos | null>(null)
  const [atendimentos, setAtendimentos] = useState<AtendimentosDashboard | null>(null)
  const [faturamento, setFaturamento] = useState<FaturamentoDashboard | null>(null)
  const [carregando, setCarregando] = useState(podeVerPaineis)

  useEffect(() => {
    if (!podeVerPaineis) return
    Promise.all([
      dashboardService.ocupacao(),
      dashboardService.atendimentos(),
      dashboardService.faturamento(),
    ])
      .then(([o, a, f]) => {
        setOcupacao(o)
        setAtendimentos(a)
        setFaturamento(f)
      })
      .finally(() => setCarregando(false))
  }, [podeVerPaineis])

  if (!podeVerPaineis) {
    return (
      <Card title={`Visão Geral - Plantão Atual`}>
        <div className="flex flex-col items-center justify-center p-8 text-center space-y-4">
          <Activity className="h-12 w-12 text-primary opacity-50" />
          <div>
            <h3 className="text-xl font-bold text-foreground">Bem-vindo(a), {usuario?.nomeCompleto}</h3>
            <p className="text-muted-foreground max-w-md mt-2">
              Seu plantão foi iniciado. Acesse o menu lateral para gerenciar a fila de atendimentos, pacientes aguardando triagem ou internações ativas.
            </p>
          </div>
        </div>
      </Card>
    )
  }

  if (carregando) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner className="h-8 w-8" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold tracking-tight text-foreground">Visão Geral Corporativa</h2>
        <Badge color="info">Dados em Tempo Real</Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <Card className="hover:shadow-md transition-shadow">
          <div className="flex flex-col space-y-1.5 p-6 pb-2">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-medium text-muted-foreground">Ocupação de Leitos</h3>
              <BedDouble className="h-4 w-4 text-muted-foreground" />
            </div>
          </div>
          <div className="p-6 pt-0">
            <div className="text-3xl font-bold text-foreground">{ocupacao?.taxaOcupacaoPercent}%</div>
            <p className="text-xs text-muted-foreground mt-1">
              <span className="font-medium text-foreground">{ocupacao?.ocupados}</span> ocupados de {ocupacao?.leitosAtivos} ativos
            </p>
            {/* Minimal Progress Bar */}
            <div className="w-full bg-secondary h-1.5 mt-4 rounded-full overflow-hidden">
              <div 
                className="bg-primary h-1.5" 
                style={{ width: `${ocupacao?.taxaOcupacaoPercent || 0}%` }}
              />
            </div>
          </div>
        </Card>

        <Card className="hover:shadow-md transition-shadow">
          <div className="flex flex-col space-y-1.5 p-6 pb-2">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-medium text-muted-foreground">Atendimentos (24h)</h3>
              <Users className="h-4 w-4 text-muted-foreground" />
            </div>
          </div>
          <div className="p-6 pt-0">
            <div className="text-3xl font-bold text-foreground">{atendimentos?.total}</div>
            <p className="text-xs text-muted-foreground mt-1">pacientes registrados no plantão</p>
          </div>
        </Card>

        <Card className="hover:shadow-md transition-shadow">
          <div className="flex flex-col space-y-1.5 p-6 pb-2">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-medium text-muted-foreground">Faturamento (Hoje)</h3>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </div>
          </div>
          <div className="p-6 pt-0">
            <div className="text-3xl font-bold text-foreground">{formatMoney(faturamento?.valorTotalGeral)}</div>
            <p className="text-xs text-muted-foreground mt-1">{faturamento?.totalContas} conta(s) fechadas hoje</p>
          </div>
        </Card>
      </div>

      <Card title="Mapa de Atendimentos">
        <div className="grid grid-cols-2 gap-4 md:grid-cols-4 lg:grid-cols-6">
          {atendimentos &&
            Object.entries(atendimentos.porStatus).map(([status, qtd]) => {
              // Clinical color mapping
              let statusColorClass = "border-border bg-background"
              if (['EM_TRIAGEM', 'AGUARDANDO_TRIAGEM'].includes(status)) statusColorClass = "border-amber-200 bg-amber-50 dark:bg-amber-950/20"
              if (['EM_ATENDIMENTO'].includes(status)) statusColorClass = "border-blue-200 bg-blue-50 dark:bg-blue-950/20"
              if (['INTERNADO'].includes(status)) statusColorClass = "border-indigo-200 bg-indigo-50 dark:bg-indigo-950/20"
              if (['ALTA'].includes(status)) statusColorClass = "border-emerald-200 bg-emerald-50 dark:bg-emerald-950/20"
              if (['CANCELADO'].includes(status)) statusColorClass = "border-rose-200 bg-rose-50 dark:bg-rose-950/20"

              return (
                <div key={status} className={`rounded-lg border p-3 flex flex-col justify-between h-24 ${statusColorClass}`}>
                  <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground truncate" title={STATUS_ATENDIMENTO_LABELS[status] ?? status}>
                    {STATUS_ATENDIMENTO_LABELS[status] ?? status}
                  </div>
                  <div className="text-2xl font-bold text-foreground">{qtd}</div>
                </div>
              )
            })}
        </div>
      </Card>
    </div>
  )
}
