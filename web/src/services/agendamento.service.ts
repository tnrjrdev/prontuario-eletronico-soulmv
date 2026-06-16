import { api } from './api'
import type { Agendamento, Page, Profissional, StatusAgendamento, TipoAgendamento } from '../types'

export interface AgendamentoFiltro {
  profissionalId?: number
  pacienteId?: number
  setorId?: number
  status?: StatusAgendamento
  tipo?: TipoAgendamento
  de?: string
  ate?: string
  page?: number
  size?: number
  sort?: string
}

export interface AgendamentoPayload {
  pacienteId: number
  profissionalId: number
  setorId: number
  convenioId?: number
  tipo: TipoAgendamento
  dataHora: string
  duracaoMinutos?: number
  observacoes?: string
}

export const agendamentoService = {
  async listar(filtro: AgendamentoFiltro = {}): Promise<Page<Agendamento>> {
    const { data } = await api.get<Page<Agendamento>>('/agendamentos', { params: filtro })
    return data
  },

  async criar(payload: AgendamentoPayload): Promise<Agendamento> {
    const { data } = await api.post<Agendamento>('/agendamentos', payload)
    return data
  },

  async atualizar(id: number, payload: AgendamentoPayload): Promise<Agendamento> {
    const { data } = await api.put<Agendamento>(`/agendamentos/${id}`, payload)
    return data
  },

  async atualizarStatus(id: number, status: StatusAgendamento): Promise<Agendamento> {
    const { data } = await api.patch<Agendamento>(`/agendamentos/${id}/status`, { status })
    return data
  },

  async checkin(id: number): Promise<Agendamento> {
    const { data } = await api.post<Agendamento>(`/agendamentos/${id}/checkin`)
    return data
  },

  async profissionais(): Promise<Profissional[]> {
    const { data } = await api.get<Profissional[]>('/usuarios/profissionais')
    return data
  },
}
