export function formatCpf(cpf: string): string {
  if (!cpf || cpf.length !== 11) return cpf
  return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}`
}

export function formatDate(iso?: string): string {
  if (!iso) return '-'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString('pt-BR')
}

export function formatDateOnly(iso?: string): string {
  if (!iso) return '-'
  const d = new Date(iso + (iso.length === 10 ? 'T00:00:00' : ''))
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleDateString('pt-BR')
}

export function formatMoney(value?: number): string {
  if (value == null) return '-'
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

/** Calcula a idade (em anos completos) a partir de uma data ISO (YYYY-MM-DD). */
export function calcularIdade(dataNascimento?: string): number | null {
  if (!dataNascimento) return null
  const nasc = new Date(dataNascimento + (dataNascimento.length === 10 ? 'T00:00:00' : ''))
  if (Number.isNaN(nasc.getTime())) return null
  const hoje = new Date()
  let idade = hoje.getFullYear() - nasc.getFullYear()
  const m = hoje.getMonth() - nasc.getMonth()
  if (m < 0 || (m === 0 && hoje.getDate() < nasc.getDate())) idade--
  return idade
}

/** Extrai apenas o horário (HH:mm) de uma data ISO. */
export function formatTime(iso?: string): string {
  if (!iso) return '-'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}
