import { describe, it, expect } from 'vitest'
import { formatCpf, formatMoney, calcularIdade } from './format'

describe('formatCpf', () => {
  it('formata 11 dígitos no padrão CPF', () => {
    expect(formatCpf('12345678901')).toBe('123.456.789-01')
  })
  it('retorna a entrada quando não tem 11 dígitos', () => {
    expect(formatCpf('123')).toBe('123')
  })
})

describe('formatMoney', () => {
  it('retorna "-" para valor nulo', () => {
    expect(formatMoney(undefined)).toBe('-')
  })
  it('formata em BRL', () => {
    expect(formatMoney(1234.5)).toContain('1.234,50')
    expect(formatMoney(1234.5)).toContain('R$')
  })
})

describe('calcularIdade', () => {
  it('retorna null para entrada ausente/ inválida', () => {
    expect(calcularIdade(undefined)).toBeNull()
    expect(calcularIdade('data-invalida')).toBeNull()
  })
  it('calcula anos completos', () => {
    const d = new Date()
    d.setFullYear(d.getFullYear() - 25)
    const iso = d.toISOString().slice(0, 10)
    expect(calcularIdade(iso)).toBe(25)
  })
})
