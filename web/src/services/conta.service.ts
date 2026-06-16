import { api } from './api'
import type { ContaHospitalar, GuiaTiss, Page, StatusConta } from '../types'

export interface ContaFiltro {
  status?: StatusConta
  page?: number
  size?: number
}

export interface ItemContaPayload {
  procedimentoId: number
  quantidade: number
  valorUnitario?: number
}

export const contaService = {
  async listar(filtro: ContaFiltro = {}): Promise<Page<ContaHospitalar>> {
    const { data } = await api.get<Page<ContaHospitalar>>('/contas', { params: filtro })
    return data
  },

  async buscar(id: number): Promise<ContaHospitalar> {
    const { data } = await api.get<ContaHospitalar>(`/contas/${id}`)
    return data
  },

  async abrir(atendimentoId: number): Promise<ContaHospitalar> {
    const { data } = await api.post<ContaHospitalar>('/contas', { atendimentoId })
    return data
  },

  async adicionarItem(id: number, payload: ItemContaPayload): Promise<ContaHospitalar> {
    const { data } = await api.post<ContaHospitalar>(`/contas/${id}/itens`, payload)
    return data
  },

  async fechar(id: number): Promise<ContaHospitalar> {
    const { data } = await api.post<ContaHospitalar>(`/contas/${id}/fechar`)
    return data
  },

  async gerarGuia(id: number): Promise<GuiaTiss> {
    const { data } = await api.post<GuiaTiss>(`/contas/${id}/guias-tiss`)
    return data
  },

  async listarGuias(id: number): Promise<GuiaTiss[]> {
    const { data } = await api.get<GuiaTiss[]>(`/contas/${id}/guias-tiss`)
    return data
  },

  async baixarXml(guiaId: number): Promise<string> {
    const { data } = await api.get(`/guias-tiss/${guiaId}/xml`, { responseType: 'text' })
    return data as string
  },
}
