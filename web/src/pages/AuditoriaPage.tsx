import { useEffect, useState } from 'react'
import { Badge, Button, Card, Input, type BadgeColor } from '../components/ui'
import { auditoriaService } from '../services/auditoria.service'
import type { LogAuditoria } from '../types'
import { formatDate } from '../utils/format'

function corStatus(status: number): BadgeColor {
  if (status >= 500) return 'red'
  if (status >= 400) return 'orange'
  if (status >= 200 && status < 300) return 'green'
  return 'slate'
}

export function AuditoriaPage() {
  const [logs, setLogs] = useState<LogAuditoria[]>([])
  const [usuario, setUsuario] = useState('')
  const [caminho, setCaminho] = useState('')

  const carregar = () => {
    auditoriaService
      .listar({ usuario: usuario || undefined, caminho: caminho || undefined, size: 50 })
      .then((p) => setLogs(p.content))
  }

  useEffect(() => {
    carregar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold text-slate-700">Trilha de auditoria</h2>
      <Card>
        <div className="mb-4 flex flex-wrap gap-2">
          <Input placeholder="Usuário (login)" value={usuario} onChange={(e) => setUsuario(e.target.value)} />
          <Input placeholder="Caminho contém..." value={caminho} onChange={(e) => setCaminho(e.target.value)} />
          <Button variant="secondary" onClick={carregar}>
            Filtrar
          </Button>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b text-left text-slate-500">
              <th className="py-2">Data/hora</th>
              <th>Usuário</th>
              <th>Método</th>
              <th>Caminho</th>
              <th>Status</th>
              <th>IP</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((l) => (
              <tr key={l.id} className="border-b last:border-0">
                <td className="py-2">{formatDate(l.dataHora)}</td>
                <td className="font-medium text-slate-700">{l.usuarioLogin}</td>
                <td>{l.metodo}</td>
                <td className="font-mono text-xs">{l.caminho}</td>
                <td>
                  <Badge color={corStatus(l.status)}>{l.status}</Badge>
                </td>
                <td className="text-xs text-slate-500">{l.ip}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
