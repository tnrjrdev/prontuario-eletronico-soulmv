import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import type { Role, Usuario } from '../types'
import { authService } from '../services/auth.service'
import { getToken, setToken } from '../services/api'

interface AuthContextValue {
  usuario: Usuario | null
  carregando: boolean
  login: (login: string, senha: string) => Promise<void>
  logout: () => void
  hasRole: (...roles: Role[]) => boolean
}

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    const token = getToken()
    if (!token) {
      setCarregando(false)
      return
    }
    authService
      .me()
      .then(setUsuario)
      .catch(() => setToken(null))
      .finally(() => setCarregando(false))
  }, [])

  const login = useCallback(async (loginValue: string, senha: string) => {
    const resposta = await authService.login(loginValue, senha)
    setToken(resposta.accessToken)
    setUsuario(resposta.usuario)
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    setUsuario(null)
  }, [])

  const hasRole = useCallback(
    (...roles: Role[]) => !!usuario && usuario.roles.some((r) => roles.includes(r)),
    [usuario],
  )

  const value = useMemo(
    () => ({ usuario, carregando, login, logout, hasRole }),
    [usuario, carregando, login, logout, hasRole],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
