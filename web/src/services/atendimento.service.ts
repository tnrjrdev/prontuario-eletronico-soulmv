import { api } from './api'
import type { Atendimento, Page } from '../types'

export interface AtendimentoFiltro {
  status?: string
  tipo?: string
  setorId?: number
  pacienteId?: number
  page?: number
  size?: number
}

export interface AtendimentoPayload {
  pacienteId: number
  tipo: string
  setorId: number
  queixaPrincipal?: string
}

export const atendimentoService = {
  async listar(filtro: AtendimentoFiltro = {}): Promise<Page<Atendimento>> {
    const { data } = await api.get<Page<Atendimento>>('/atendimentos', { params: filtro })
    return data
  },

  async buscar(id: number): Promise<Atendimento> {
    const { data } = await api.get<Atendimento>(`/atendimentos/${id}`)
    return data
  },

  async abrir(payload: AtendimentoPayload): Promise<Atendimento> {
    const { data } = await api.post<Atendimento>('/atendimentos', payload)
    return data
  },

  async atualizarStatus(id: number, status: string): Promise<Atendimento> {
    const { data } = await api.patch<Atendimento>(`/atendimentos/${id}/status`, { status })
    return data
  },

  async alocarLeito(id: number, leitoId: number): Promise<Atendimento> {
    const { data } = await api.patch<Atendimento>(`/atendimentos/${id}/leito`, { leitoId })
    return data
  },

  async darAlta(id: number): Promise<Atendimento> {
    const { data } = await api.post<Atendimento>(`/atendimentos/${id}/alta`)
    return data
  },
}
