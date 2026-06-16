/**
 * Gerência do tema (claro/escuro) com persistência em localStorage.
 * Aplicado cedo em main.tsx (applyStoredTheme) para evitar flash de tema errado.
 */
export type Theme = 'light' | 'dark'

const STORAGE_KEY = 'pep.theme'

export function getStoredTheme(): Theme {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved === 'light' || saved === 'dark') return saved
  // fallback: preferência do sistema
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function applyTheme(theme: Theme) {
  document.documentElement.classList.toggle('dark', theme === 'dark')
}

export function setTheme(theme: Theme) {
  localStorage.setItem(STORAGE_KEY, theme)
  applyTheme(theme)
}

/** Aplica o tema salvo no boot da aplicação. */
export function applyStoredTheme() {
  applyTheme(getStoredTheme())
}
