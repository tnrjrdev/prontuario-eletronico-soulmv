import { api } from './api'
import type {
  Anamnese,
  Diagnostico,
  Evolucao,
  Prescricao,
  SinaisVitais,
  SolicitacaoExame,
  ViaAdministracao,
} from '../types'

export interface ItemPrescricaoPayload {
  medicamentoId: number
  dose: string
  via?: ViaAdministracao
  frequencia?: string
  duracao?: string
  observacao?: string
}

export interface PrescricaoPayload {
  observacao?: string
  itens: ItemPrescricaoPayload[]
}

/**
 * Serviços do prontuário clínico (PEP), todos aninhados sob um atendimento.
 * Os endpoints espelham os controllers do backend (EvolucaoController,
 * PrescricaoController, SinaisVitaisController, AnamneseController,
 * DiagnosticoController e ExameController).
 */
export const clinicoService = {
  async anamnese(atendimentoId: number): Promise<Anamnese | null> {
    // O backend retorna 404 quando ainda não há anamnese registrada.
    try {
      const { data } = await api.get<Anamnese>(`/atendimentos/${atendimentoId}/anamnese`)
      return data
    } catch {
      return null
    }
  },

  async diagnosticos(atendimentoId: number): Promise<Diagnostico[]> {
    const { data } = await api.get<Diagnostico[]>(`/atendimentos/${atendimentoId}/diagnosticos`)
    return data
  },

  async sinaisVitais(atendimentoId: number): Promise<SinaisVitais[]> {
    const { data } = await api.get<SinaisVitais[]>(`/atendimentos/${atendimentoId}/sinais-vitais`)
    return data
  },

  async evolucoes(atendimentoId: number): Promise<Evolucao[]> {
    const { data } = await api.get<Evolucao[]>(`/atendimentos/${atendimentoId}/evolucoes`)
    return data
  },

  async registrarEvolucao(atendimentoId: number, texto: string): Promise<Evolucao> {
    const { data } = await api.post<Evolucao>(`/atendimentos/${atendimentoId}/evolucoes`, { texto })
    return data
  },

  async prescricoes(atendimentoId: number): Promise<Prescricao[]> {
    const { data } = await api.get<Prescricao[]>(`/atendimentos/${atendimentoId}/prescricoes`)
    return data
  },

  async criarPrescricao(atendimentoId: number, payload: PrescricaoPayload): Promise<Prescricao> {
    const { data } = await api.post<Prescricao>(`/atendimentos/${atendimentoId}/prescricoes`, payload)
    return data
  },

  async exames(atendimentoId: number): Promise<SolicitacaoExame[]> {
    const { data } = await api.get<SolicitacaoExame[]>(`/atendimentos/${atendimentoId}/exames`)
    return data
  },
}
