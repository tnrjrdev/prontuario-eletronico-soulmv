import { describe, it, expect } from 'vitest'
import { tokensDeAlergia, analisarMedicamento, temAlertaCritico } from './cds'

describe('tokensDeAlergia', () => {
  it('extrai termos relevantes e ignora stopwords', () => {
    const tokens = tokensDeAlergia('Alergia a Dipirona e Penicilina')
    expect(tokens).toContain('dipirona')
    expect(tokens).toContain('penicilina')
    expect(tokens).not.toContain('alergia')
  })

  it('retorna vazio para nulo/curto', () => {
    expect(tokensDeAlergia(null)).toEqual([])
    expect(tokensDeAlergia('')).toEqual([])
  })
})

describe('analisarMedicamento', () => {
  it('alerta crítico de ALERGIA quando o medicamento bate com a alergia', () => {
    const alertas = analisarMedicamento(
      { nome: 'Dipirona Monoidratada', principioAtivo: 'dipirona' },
      { alergiaTokens: ['dipirona'], nomesAtivos: [] },
    )
    expect(alertas.some((a) => a.tipo === 'ALERGIA' && a.severidade === 'critico')).toBe(true)
    expect(temAlertaCritico(alertas)).toBe(true)
  })

  it('alerta de DUPLICIDADE quando já existe item ativo com o mesmo nome', () => {
    const alertas = analisarMedicamento(
      { nome: 'Omeprazol' },
      { alergiaTokens: [], nomesAtivos: ['omeprazol'] },
    )
    expect(alertas.some((a) => a.tipo === 'DUPLICIDADE')).toBe(true)
  })

  it('alerta de CONTROLADO para medicamento controlado', () => {
    const alertas = analisarMedicamento(
      { nome: 'Morfina', controlado: true },
      { alergiaTokens: [], nomesAtivos: [] },
    )
    expect(alertas.some((a) => a.tipo === 'CONTROLADO')).toBe(true)
  })

  it('sem alertas quando não há alergia, duplicidade nem controle', () => {
    const alertas = analisarMedicamento(
      { nome: 'Paracetamol' },
      { alergiaTokens: ['dipirona'], nomesAtivos: ['omeprazol'] },
    )
    expect(alertas).toHaveLength(0)
    expect(temAlertaCritico(alertas)).toBe(false)
  })
})
