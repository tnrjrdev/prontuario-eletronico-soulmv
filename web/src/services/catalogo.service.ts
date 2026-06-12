import { api } from './api'
import type { Convenio, Page, Setor } from '../types'

export const catalogoService = {
  async setores(): Promise<Setor[]> {
    const { data } = await api.get<Page<Setor>>('/setores', { params: { size: 100 } })
    return data.content
  },

  async convenios(): Promise<Convenio[]> {
    const { data } = await api.get<Page<Convenio>>('/convenios', { params: { size: 100 } })
    return data.content
  },
}
