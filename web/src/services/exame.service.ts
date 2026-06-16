import { api } from './api'
import type { Page, SolicitacaoExame, StatusExame } from '../types'

export interface ExameFiltro {
  status?: StatusExame
  pacienteId?: number
  page?: number
  size?: number
}

export const exameService = {
  async listar(filtro: ExameFiltro = {}): Promise<Page<SolicitacaoExame>> {
    const { data } = await api.get<Page<SolicitacaoExame>>('/exames', { params: filtro })
    return data
  },

  async atualizarStatus(id: number, status: StatusExame): Promise<SolicitacaoExame> {
    const { data } = await api.patch<SolicitacaoExame>(`/exames/${id}/status`, { status })
    return data
  },

  /** Baixa o arquivo do laudo (PDF/imagem) e dispara o download no navegador. */
  async baixarLaudo(id: number, nomeSugerido = `laudo-exame-${id}`): Promise<void> {
    const resp = await api.get(`/exames/${id}/laudo`, { responseType: 'blob' })
    const url = URL.createObjectURL(resp.data as Blob)
    const a = document.createElement('a')
    a.href = url
    a.download = nomeSugerido
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  },
}
