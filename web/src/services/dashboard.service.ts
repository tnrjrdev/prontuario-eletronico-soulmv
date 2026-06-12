import { api } from './api'
import type { AtendimentosDashboard, FaturamentoDashboard, OcupacaoLeitos } from '../types'

export const dashboardService = {
  async ocupacao(): Promise<OcupacaoLeitos> {
    const { data } = await api.get<OcupacaoLeitos>('/dashboards/ocupacao')
    return data
  },
  async atendimentos(): Promise<AtendimentosDashboard> {
    const { data } = await api.get<AtendimentosDashboard>('/dashboards/atendimentos')
    return data
  },
  async faturamento(): Promise<FaturamentoDashboard> {
    const { data } = await api.get<FaturamentoDashboard>('/dashboards/faturamento')
    return data
  },
}
