/**
 * CDS (Clinical Decision Support) — verificações de segurança na prescrição.
 *
 * Escopo possível com os dados atuais: alergia (cruza o medicamento com o texto
 * de alergias da anamnese), duplicidade (item já ativo) e medicamento controlado.
 * Interações medicamentosas exigiriam uma base externa (Brasíndice/Micromedex)
 * e ficam como evolução — a estrutura abaixo já comporta novos tipos de alerta.
 */

export type CdsSeveridade = 'critico' | 'alerta'
export type CdsTipo = 'ALERGIA' | 'DUPLICIDADE' | 'CONTROLADO'

export interface CdsAlerta {
  tipo: CdsTipo
  severidade: CdsSeveridade
  mensagem: string
}

export interface MedicamentoCds {
  nome: string
  principioAtivo?: string
  controlado?: boolean
}

const STOPWORDS = new Set([
  'alergia', 'alergias', 'alergico', 'alergica', 'paciente', 'sem', 'nao', 'não',
  'medicamento', 'medicamentos', 'relata', 'refere', 'conhecida', 'conhecidas',
  'com', 'sem', 'para', 'dos', 'das', 'que', 'uso',
])

/** Extrai termos relevantes do texto livre de alergias da anamnese. */
export function tokensDeAlergia(alergias?: string | null): string[] {
  if (!alergias) return []
  return alergias
    .toLowerCase()
    .split(/[^a-zà-ú0-9]+/i)
    .map((t) => t.trim())
    .filter((t) => t.length >= 3 && !STOPWORDS.has(t))
}

/**
 * Avalia um medicamento contra o contexto clínico do paciente.
 * @param med medicamento a prescrever
 * @param ctx alergiaTokens (de tokensDeAlergia) e nomesAtivos (itens já ativos)
 */
export function analisarMedicamento(
  med: MedicamentoCds,
  ctx: { alergiaTokens: string[]; nomesAtivos: string[] }
): CdsAlerta[] {
  const alvo = `${med.nome} ${med.principioAtivo ?? ''}`.toLowerCase()
  const alertas: CdsAlerta[] = []

  const termo = ctx.alergiaTokens.find((t) => alvo.includes(t))
  if (termo) {
    alertas.push({
      tipo: 'ALERGIA',
      severidade: 'critico',
      mensagem: `Alergia registrada compatível com "${termo}". Confirme antes de prescrever.`,
    })
  }

  if (ctx.nomesAtivos.some((n) => n.toLowerCase() === med.nome.toLowerCase())) {
    alertas.push({
      tipo: 'DUPLICIDADE',
      severidade: 'alerta',
      mensagem: 'Já existe um item ativo com este medicamento (possível duplicidade).',
    })
  }

  if (med.controlado) {
    alertas.push({
      tipo: 'CONTROLADO',
      severidade: 'alerta',
      mensagem: 'Medicamento controlado — exige receituário especial.',
    })
  }

  return alertas
}

export const temAlertaCritico = (alertas: CdsAlerta[]) => alertas.some((a) => a.severidade === 'critico')
