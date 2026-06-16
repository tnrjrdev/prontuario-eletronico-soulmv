import { api } from './api'
import type { Leito, Page, StatusLeito } from '../types'

export const leitoService = {
  async listar(setorId?: number): Promise<Leito[]> {
    const { data } = await api.get<Page<Leito>>('/leitos', { params: { setorId, size: 200 } })
    return data.content
  },

  async atualizarStatus(id: number, status: StatusLeito): Promise<Leito> {
    const { data } = await api.patch<Leito>(`/leitos/${id}/status`, { status })
    return data
  },
}
