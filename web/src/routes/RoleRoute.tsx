import type { ReactNode } from 'react'
import { useAuth } from '../hooks/useAuth'
import type { Role } from '../types'

export function RoleRoute({ roles, children }: { roles: Role[]; children: ReactNode }) {
  const { hasRole } = useAuth()
  if (!hasRole(...roles)) {
    return (
      <div className="rounded-md bg-yellow-50 border border-yellow-200 p-6 text-yellow-800">
        Você não tem permissão para acessar esta área.
      </div>
    )
  }
  return <>{children}</>
}
