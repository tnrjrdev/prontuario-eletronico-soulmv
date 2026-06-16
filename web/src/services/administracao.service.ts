import { api } from './api'
import type { Administracao, StatusAdministracao } from '../types'

export const administracaoService = {
  async listar(itemId: number): Promise<Administracao[]> {
    const { data } = await api.get<Administracao[]>(`/itens-prescricao/${itemId}/administracoes`)
    return data
  },

  async registrar(itemId: number, status: StatusAdministracao, observacao?: string): Promise<Administracao> {
    const { data } = await api.post<Administracao>(`/itens-prescricao/${itemId}/administracoes`, { status, observacao })
    return data
  },
}
