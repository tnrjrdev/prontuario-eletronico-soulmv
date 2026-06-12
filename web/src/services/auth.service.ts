import { api } from './api'
import type { TokenResponse, Usuario } from '../types'

export const authService = {
  async login(login: string, senha: string): Promise<TokenResponse> {
    const { data } = await api.post<TokenResponse>('/auth/login', { login, senha })
    return data
  },

  async me(): Promise<Usuario> {
    const { data } = await api.get<Usuario>('/auth/me')
    return data
  },
}
