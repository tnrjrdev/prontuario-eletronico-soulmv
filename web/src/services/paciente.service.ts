import { api } from './api'
import type { Page, Paciente } from '../types'

export interface PacienteFiltro {
  nome?: string
  cpf?: string
  page?: number
  size?: number
}

export interface PacientePayload {
  nome: string
  cpf: string
  dataNascimento: string
  sexo?: string
  telefone?: string
  email?: string
  convenioId?: number | null
  numeroCarteirinha?: string
}

export const pacienteService = {
  async listar(filtro: PacienteFiltro = {}): Promise<Page<Paciente>> {
    const { data } = await api.get<Page<Paciente>>('/pacientes', { params: filtro })
    return data
  },

  async buscar(id: number): Promise<Paciente> {
    const { data } = await api.get<Paciente>(`/pacientes/${id}`)
    return data
  },

  async criar(payload: PacientePayload): Promise<Paciente> {
    const { data } = await api.post<Paciente>('/pacientes', payload)
    return data
  },

  async atualizar(id: number, payload: PacientePayload): Promise<Paciente> {
    const { data } = await api.put<Paciente>(`/pacientes/${id}`, payload)
    return data
  },
}
