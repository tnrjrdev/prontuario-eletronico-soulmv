import type { Role } from '../types'

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: 'Administrador',
  MEDICO: 'Médico',
  ENFERMEIRO: 'Enfermagem',
  RECEPCAO: 'Recepção',
  FATURAMENTO: 'Faturamento',
  PACIENTE: 'Paciente',
}

export const STATUS_ATENDIMENTO_LABELS: Record<string, string> = {
  AGUARDANDO_TRIAGEM: 'Aguardando triagem',
  EM_TRIAGEM: 'Em triagem',
  AGUARDANDO_ATENDIMENTO: 'Aguardando atendimento',
  EM_ATENDIMENTO: 'Em atendimento',
  INTERNADO: 'Internado',
  AGUARDANDO_EXAME: 'Aguardando exame',
  ALTA: 'Alta',
  CANCELADO: 'Cancelado',
}
