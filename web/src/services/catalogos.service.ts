import { api } from './api'
import type {
  Cid10, Convenio, Medicamento, Page, ProcedimentoTuss, Setor, TipoConvenio, TipoSetor,
} from '../types'

export interface SetorPayload { nome: string; tipo: TipoSetor; descricao?: string }
export interface ConvenioPayload { nome: string; registroAns?: string; tipo: TipoConvenio }
export interface MedicamentoPayload { nome: string; principioAtivo?: string; concentracao?: string; controlado: boolean }
export interface ProcedimentoPayload { codigoTuss: string; descricao: string; valorReferencia?: number }
export interface Cid10Payload { codigo: string; descricao: string }

async function listar<T>(base: string): Promise<T[]> {
  const { data } = await api.get<Page<T>>(base, { params: { size: 500 } })
  return data.content
}

export const setorService = {
  listar: () => listar<Setor>('/setores'),
  criar: async (p: SetorPayload) => (await api.post<Setor>('/setores', p)).data,
  atualizar: async (id: number, p: SetorPayload) => (await api.put<Setor>(`/setores/${id}`, p)).data,
  atualizarStatus: async (id: number, ativo: boolean) => (await api.patch<Setor>(`/setores/${id}/status`, { ativo })).data,
}

export const convenioService = {
  listar: () => listar<Convenio>('/convenios'),
  criar: async (p: ConvenioPayload) => (await api.post<Convenio>('/convenios', p)).data,
  atualizar: async (id: number, p: ConvenioPayload) => (await api.put<Convenio>(`/convenios/${id}`, p)).data,
  atualizarStatus: async (id: number, ativo: boolean) => (await api.patch<Convenio>(`/convenios/${id}/status`, { ativo })).data,
}

export const medicamentoService = {
  listar: () => listar<Medicamento>('/medicamentos'),
  /** Busca por nome/princípio ativo (typeahead). */
  buscar: async (q: string): Promise<Medicamento[]> => {
    const { data } = await api.get<Page<Medicamento>>('/medicamentos', { params: { q, size: 20 } })
    return data.content.filter((m) => m.ativo)
  },
  criar: async (p: MedicamentoPayload) => (await api.post<Medicamento>('/medicamentos', p)).data,
  atualizar: async (id: number, p: MedicamentoPayload) => (await api.put<Medicamento>(`/medicamentos/${id}`, p)).data,
  atualizarStatus: async (id: number, ativo: boolean) => (await api.patch<Medicamento>(`/medicamentos/${id}/status`, { ativo })).data,
}

export const procedimentoService = {
  listar: () => listar<ProcedimentoTuss>('/procedimentos-tuss'),
  criar: async (p: ProcedimentoPayload) => (await api.post<ProcedimentoTuss>('/procedimentos-tuss', p)).data,
  atualizar: async (id: number, p: ProcedimentoPayload) => (await api.put<ProcedimentoTuss>(`/procedimentos-tuss/${id}`, p)).data,
  atualizarStatus: async (id: number, ativo: boolean) => (await api.patch<ProcedimentoTuss>(`/procedimentos-tuss/${id}/status`, { ativo })).data,
}

export const cid10Service = {
  listar: () => listar<Cid10>('/cid10'),
  criar: async (p: Cid10Payload) => (await api.post<Cid10>('/cid10', p)).data,
  atualizar: async (id: number, p: Cid10Payload) => (await api.put<Cid10>(`/cid10/${id}`, p)).data,
  excluir: async (id: number) => { await api.delete(`/cid10/${id}`) },
}
