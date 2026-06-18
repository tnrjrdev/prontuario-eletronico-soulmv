import axios, { type InternalAxiosRequestConfig } from 'axios'

const TOKEN_KEY = 'hospitalar.accessToken'
const REFRESH_KEY = 'hospitalar.refreshToken'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
  headers: { 'Content-Type': 'application/json' },
})

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_KEY)
}

export function setRefreshToken(token: string | null) {
  if (token) localStorage.setItem(REFRESH_KEY, token)
  else localStorage.removeItem(REFRESH_KEY)
}

export function clearTokens() {
  setToken(null)
  setRefreshToken(null)
}

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// --- Refresh transparente de access token (single-flight) ---
let refreshing: Promise<string | null> | null = null

async function renovarAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return null
  try {
    // Chamada "crua" (sem interceptors) para evitar recursão de 401.
    const { data } = await axios.post<{ accessToken: string; refreshToken?: string }>(
      `${api.defaults.baseURL}/auth/refresh`,
      { refreshToken },
      { headers: { 'Content-Type': 'application/json' } }
    )
    setToken(data.accessToken)
    if (data.refreshToken) setRefreshToken(data.refreshToken)
    return data.accessToken
  } catch {
    return null
  }
}

function redirecionarParaLogin() {
  clearTokens()
  if (!location.pathname.startsWith('/login')) location.href = '/login'
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    const status = error.response?.status
    const url = original?.url ?? ''
    const ehChamadaAuth = url.includes('/auth/login') || url.includes('/auth/refresh')

    if (status === 401 && original && !original._retry && !ehChamadaAuth) {
      original._retry = true
      // Reaproveita um único refresh em voo para várias requisições concorrentes.
      refreshing = refreshing ?? renovarAccessToken()
      const novoToken = await refreshing
      refreshing = null

      if (novoToken) {
        original.headers = original.headers ?? {}
        original.headers.Authorization = `Bearer ${novoToken}`
        return api(original)
      }
      redirecionarParaLogin()
    }
    return Promise.reject(error)
  },
)

/** Extrai a mensagem de erro do padrão ProblemDetail do backend. */
export function extractError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { detail?: string; title?: string } | undefined
    return data?.detail ?? data?.title ?? error.message
  }
  return 'Erro inesperado'
}
