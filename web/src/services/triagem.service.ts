import { api } from './api'
import type { ClassificacaoRisco } from '../types'

export interface SinaisVitaisPayload {
  pressaoSistolica?: number
  pressaoDiastolica?: number
  frequenciaCardiaca?: number
  frequenciaRespiratoria?: number
  temperatura?: number
  saturacaoO2?: number
  glicemia?: number
  escalaDor?: number
}

export const triagemService = {
  async registrar(atendimentoId: number, payload: { classificacaoRisco: ClassificacaoRisco; observacao?: string }) {
    const { data } = await api.post(`/atendimentos/${atendimentoId}/triagem`, payload)
    return data
  },

  async registrarSinaisVitais(atendimentoId: number, payload: SinaisVitaisPayload) {
    const { data } = await api.post(`/atendimentos/${atendimentoId}/sinais-vitais`, payload)
    return data
  },
}
