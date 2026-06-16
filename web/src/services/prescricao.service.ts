import { api } from './api'
import type { Page, Prescricao, StatusPrescricao } from '../types'

export interface PrescricaoFiltro {
  status?: StatusPrescricao
  pacienteId?: number
  page?: number
  size?: number
}

export const prescricaoService = {
  async listar(filtro: PrescricaoFiltro = {}): Promise<Page<Prescricao>> {
    const { data } = await api.get<Page<Prescricao>>('/prescricoes', { params: filtro })
    return data
  },

  async atualizarStatus(id: number, status: StatusPrescricao): Promise<Prescricao> {
    const { data } = await api.patch<Prescricao>(`/prescricoes/${id}/status`, { status })
    return data
  },
}
