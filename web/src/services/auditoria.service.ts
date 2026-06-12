import { api } from './api'
import type { LogAuditoria, Page } from '../types'

export interface AuditoriaFiltro {
  usuario?: string
  caminho?: string
  page?: number
  size?: number
}

export const auditoriaService = {
  async listar(filtro: AuditoriaFiltro = {}): Promise<Page<LogAuditoria>> {
    const { data } = await api.get<Page<LogAuditoria>>('/auditoria', { params: filtro })
    return data
  },
}
