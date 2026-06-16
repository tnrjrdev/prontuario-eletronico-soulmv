// Tipos espelhando os DTOs do backend.

export type Role = 'ADMIN' | 'MEDICO' | 'ENFERMEIRO' | 'RECEPCAO' | 'FATURAMENTO' | 'PACIENTE'

export interface Usuario {
  id: number
  nomeCompleto: string
  login: string
  email: string
  ativo: boolean
  roles: Role[]
  criadoEm?: string
  atualizadoEm?: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  usuario: Usuario
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export type TipoConvenio = 'PARTICULAR' | 'PLANO_SAUDE' | 'SUS'

export interface Convenio {
  id: number
  nome: string
  registroAns?: string
  tipo: TipoConvenio
  ativo: boolean
}

export type TipoSetor =
  | 'AMBULATORIO'
  | 'EMERGENCIA'
  | 'INTERNACAO'
  | 'UTI'
  | 'CENTRO_CIRURGICO'
  | 'APOIO_DIAGNOSTICO'
  | 'ADMINISTRATIVO'

export interface Medicamento {
  id: number
  nome: string
  principioAtivo?: string
  concentracao?: string
  controlado: boolean
  ativo: boolean
}

export interface ProcedimentoTuss {
  id: number
  codigoTuss: string
  descricao: string
  valorReferencia?: number
  ativo: boolean
}

export interface Cid10 {
  id: number
  codigo: string
  descricao: string
}

export interface Endereco {
  logradouro?: string
  numero?: string
  complemento?: string
  bairro?: string
  cidade?: string
  uf?: string
  cep?: string
}

export type Sexo = 'MASCULINO' | 'FEMININO' | 'OUTRO' | 'NAO_INFORMADO'

export interface Paciente {
  id: number
  nome: string
  cpf: string
  cartaoSus?: string
  dataNascimento: string
  sexo?: Sexo
  telefone?: string
  email?: string
  endereco?: Endereco
  convenioId?: number
  convenioNome?: string
  numeroCarteirinha?: string
}

export type TipoAtendimento = 'AMBULATORIAL' | 'EMERGENCIA' | 'INTERNACAO'

export type StatusAtendimento =
  | 'AGUARDANDO_TRIAGEM'
  | 'EM_TRIAGEM'
  | 'AGUARDANDO_ATENDIMENTO'
  | 'EM_ATENDIMENTO'
  | 'INTERNADO'
  | 'AGUARDANDO_EXAME'
  | 'ALTA'
  | 'CANCELADO'

export interface Atendimento {
  id: number
  pacienteId: number
  pacienteNome: string
  tipo: TipoAtendimento
  status: StatusAtendimento
  setorId: number
  setorNome: string
  leitoId?: number
  leitoIdentificador?: string
  profissionalId?: number
  profissionalNome?: string
  queixaPrincipal?: string
  dataEntrada: string
  dataAlta?: string
}

export interface Setor {
  id: number
  nome: string
  tipo: TipoSetor
  descricao?: string
  ativo: boolean
}

export type StatusLeito = 'LIVRE' | 'OCUPADO' | 'MANUTENCAO' | 'HIGIENIZACAO' | 'INTERDITADO'

export interface Leito {
  id: number
  identificador: string
  setorId: number
  setorNome: string
  status: StatusLeito
  ativo: boolean
  criadoEm?: string
  atualizadoEm?: string
}

export interface Profissional {
  id: number
  nome: string
  roles: Role[]
}

// ---------------------------------------------------------------------------
// Agenda
// ---------------------------------------------------------------------------

export type TipoAgendamento = 'CONSULTA' | 'RETORNO' | 'EXAME' | 'PROCEDIMENTO'

export type StatusAgendamento = 'AGENDADO' | 'CONFIRMADO' | 'REALIZADO' | 'CANCELADO' | 'FALTOU'

export interface Agendamento {
  id: number
  pacienteId: number
  pacienteNome: string
  profissionalId: number
  profissionalNome: string
  setorId: number
  setorNome: string
  convenioId?: number
  convenioNome?: string
  tipo: TipoAgendamento
  status: StatusAgendamento
  dataHora: string
  duracaoMinutos: number
  observacoes?: string
  atendimentoId?: number
  criadoEm?: string
  atualizadoEm?: string
}

export interface OcupacaoLeitos {
  totalLeitos: number
  leitosAtivos: number
  ocupados: number
  livres: number
  taxaOcupacaoPercent: number
  porStatus: Record<string, number>
}

export interface AtendimentosDashboard {
  total: number
  porStatus: Record<string, number>
}

export interface FaturamentoDashboard {
  totalContas: number
  valorTotalGeral: number
  contasPorStatus: Record<string, number>
  valorPorStatus: Record<string, number>
}

export interface LogAuditoria {
  id: number
  usuarioLogin: string
  metodo: string
  caminho: string
  status: number
  ip: string
  dataHora: string
}

// ---------------------------------------------------------------------------
// Entidades clínicas (espelham os DTOs de response do backend)
// ---------------------------------------------------------------------------

export interface Anamnese {
  id: number
  atendimentoId: number
  medicoId: number
  medicoNome: string
  historiaDoencaAtual?: string
  antecedentes?: string
  alergias?: string
  exameFisico?: string
  dataHora: string
}

export type TipoDiagnostico = 'PRINCIPAL' | 'SECUNDARIO'

export interface Diagnostico {
  id: number
  atendimentoId: number
  cid10Id: number
  cid10Codigo: string
  cid10Descricao: string
  tipo: TipoDiagnostico
  medicoId: number
  medicoNome: string
  observacao?: string
  dataHora: string
}

export interface SinaisVitais {
  id: number
  atendimentoId: number
  pressaoSistolica?: number
  pressaoDiastolica?: number
  frequenciaCardiaca?: number
  frequenciaRespiratoria?: number
  temperatura?: number
  saturacaoO2?: number
  glicemia?: number
  escalaDor?: number
  registradoPorId?: number
  registradoPorNome?: string
  dataHora: string
}

export type TipoEvolucao = 'MEDICA' | 'ENFERMAGEM'

export interface Evolucao {
  id: number
  atendimentoId: number
  tipo: TipoEvolucao
  texto: string
  autorId: number
  autorNome: string
  dataHora: string
}

export type StatusPrescricao = 'ATIVA' | 'SUSPENSA' | 'ENCERRADA'

export type ViaAdministracao =
  | 'ORAL'
  | 'INTRAVENOSA'
  | 'INTRAMUSCULAR'
  | 'SUBCUTANEA'
  | 'TOPICA'
  | 'INALATORIA'
  | 'RETAL'
  | 'OUTRA'

export interface ItemPrescricao {
  id: number
  medicamentoId: number
  medicamentoNome: string
  medicamentoControlado: boolean
  dose: string
  via: ViaAdministracao
  frequencia: string
  duracao?: string
  observacao?: string
}

export interface Prescricao {
  id: number
  atendimentoId: number
  medicoId: number
  medicoNome: string
  status: StatusPrescricao
  observacao?: string
  dataHora: string
  itens: ItemPrescricao[]
}

export type StatusExame =
  | 'SOLICITADO'
  | 'COLETADO'
  | 'EM_ANALISE'
  | 'LIBERADO'
  | 'CANCELADO'

export interface ResultadoExame {
  id: number
  resultadoTexto?: string
  laudoAnexoId?: number
  temLaudo: boolean
  liberadoPorId?: number
  liberadoPorNome?: string
  dataLiberacao?: string
}

export interface SolicitacaoExame {
  id: number
  atendimentoId: number
  tipoExame: string
  status: StatusExame
  observacao?: string
  medicoSolicitanteId?: number
  medicoSolicitanteNome?: string
  dataSolicitacao: string
  resultado?: ResultadoExame
}
