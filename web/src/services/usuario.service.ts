import { api } from './api'
import type { Page, Role, Usuario } from '../types'

export interface UsuarioPayload {
  nomeCompleto: string
  login: string
  email: string
  senha: string
  roles: Role[]
}

export const usuarioService = {
  async listar(page = 0, size = 20): Promise<Page<Usuario>> {
    const { data } = await api.get<Page<Usuario>>('/usuarios', { params: { page, size } })
    return data
  },

  async criar(payload: UsuarioPayload): Promise<Usuario> {
    const { data } = await api.post<Usuario>('/usuarios', payload)
    return data
  },

  async atualizarStatus(id: number, ativo: boolean): Promise<Usuario> {
    const { data } = await api.patch<Usuario>(`/usuarios/${id}/status`, { ativo })
    return data
  },
}
