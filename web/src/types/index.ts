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

export interface Convenio {
  id: number
  nome: string
  tipo: string
  ativo: boolean
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
  tipo: string
  ativo: boolean
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
