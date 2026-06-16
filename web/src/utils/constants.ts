import type {
  Role, Sexo, StatusExame, ViaAdministracao,
  StatusLeito, TipoAgendamento, StatusAgendamento,
  TipoSetor, TipoConvenio, StatusConta, StatusAdministracao,
} from '../types'

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

export const SEXO_LABELS: Record<Sexo, string> = {
  MASCULINO: 'Masculino',
  FEMININO: 'Feminino',
  OUTRO: 'Outro',
  NAO_INFORMADO: 'Não informado',
}

/** Abreviação clínica usual da via de administração (ex.: VO, IV). */
export const VIA_ADMINISTRACAO_LABELS: Record<ViaAdministracao, string> = {
  ORAL: 'VO',
  INTRAVENOSA: 'IV',
  INTRAMUSCULAR: 'IM',
  SUBCUTANEA: 'SC',
  TOPICA: 'Tópica',
  INALATORIA: 'Inalatória',
  RETAL: 'Retal',
  OUTRA: 'Outra',
}

export const STATUS_EXAME_LABELS: Record<StatusExame, string> = {
  SOLICITADO: 'Solicitado',
  COLETADO: 'Coletado',
  EM_ANALISE: 'Em análise',
  LIBERADO: 'Liberado',
  CANCELADO: 'Cancelado',
}

export const STATUS_LEITO_LABELS: Record<StatusLeito, string> = {
  LIVRE: 'Livre',
  OCUPADO: 'Ocupado',
  MANUTENCAO: 'Manutenção',
  HIGIENIZACAO: 'Higienização',
  INTERDITADO: 'Interditado',
}

export const TIPO_AGENDAMENTO_LABELS: Record<TipoAgendamento, string> = {
  CONSULTA: 'Consulta',
  RETORNO: 'Retorno',
  EXAME: 'Exame',
  PROCEDIMENTO: 'Procedimento',
}

export const STATUS_AGENDAMENTO_LABELS: Record<StatusAgendamento, string> = {
  AGENDADO: 'Agendado',
  CONFIRMADO: 'Confirmado',
  REALIZADO: 'Realizado',
  CANCELADO: 'Cancelado',
  FALTOU: 'Faltou',
}

export const TIPO_SETOR_LABELS: Record<TipoSetor, string> = {
  AMBULATORIO: 'Ambulatório',
  EMERGENCIA: 'Emergência',
  INTERNACAO: 'Internação',
  UTI: 'UTI',
  CENTRO_CIRURGICO: 'Centro cirúrgico',
  APOIO_DIAGNOSTICO: 'Apoio diagnóstico',
  ADMINISTRATIVO: 'Administrativo',
}

export const TIPO_CONVENIO_LABELS: Record<TipoConvenio, string> = {
  PARTICULAR: 'Particular',
  PLANO_SAUDE: 'Plano de saúde',
  SUS: 'SUS',
}

export const STATUS_CONTA_LABELS: Record<StatusConta, string> = {
  ABERTA: 'Aberta',
  FECHADA: 'Fechada',
  FATURADA: 'Faturada',
  GLOSADA: 'Glosada',
  CANCELADA: 'Cancelada',
}

export const STATUS_ADMINISTRACAO_LABELS: Record<StatusAdministracao, string> = {
  ADMINISTRADO: 'Administrado',
  RECUSADO: 'Recusado',
  NAO_ADMINISTRADO: 'Não administrado',
}
