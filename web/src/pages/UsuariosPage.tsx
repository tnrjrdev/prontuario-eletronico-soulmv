import { useEffect, useState, type FormEvent } from 'react'
import { Alert, Badge, Button, Card, Input } from '../components/ui'
import { usuarioService, type UsuarioPayload } from '../services/usuario.service'
import { extractError } from '../services/api'
import type { Role, Usuario } from '../types'
import { ROLE_LABELS } from '../utils/constants'

const TODAS_ROLES: Role[] = ['ADMIN', 'MEDICO', 'ENFERMEIRO', 'RECEPCAO', 'FATURAMENTO', 'PACIENTE']
const VAZIO: UsuarioPayload = { nomeCompleto: '', login: '', email: '', senha: '', roles: [] }

export function UsuariosPage() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([])
  const [form, setForm] = useState<UsuarioPayload>(VAZIO)
  const [mostrarForm, setMostrarForm] = useState(false)
  const [erro, setErro] = useState('')

  const carregar = () => usuarioService.listar(0, 100).then((p) => setUsuarios(p.content))

  useEffect(() => {
    carregar()
  }, [])

  const toggleRole = (role: Role) => {
    setForm((f) => ({
      ...f,
      roles: f.roles.includes(role) ? f.roles.filter((r) => r !== role) : [...f.roles, role],
    }))
  }

  const salvar = async (e: FormEvent) => {
    e.preventDefault()
    setErro('')
    try {
      await usuarioService.criar(form)
      setForm(VAZIO)
      setMostrarForm(false)
      carregar()
    } catch (err) {
      setErro(extractError(err))
    }
  }

  const alternarStatus = async (u: Usuario) => {
    await usuarioService.atualizarStatus(u.id, !u.ativo)
    carregar()
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-700">Usuários</h2>
        <Button onClick={() => setMostrarForm((v) => !v)}>{mostrarForm ? 'Fechar' : 'Novo usuário'}</Button>
      </div>

      {mostrarForm && (
        <Card title="Novo usuário">
          <form onSubmit={salvar} className="grid grid-cols-1 gap-4 md:grid-cols-2">
            {erro && (
              <div className="md:col-span-2">
                <Alert>{erro}</Alert>
              </div>
            )}
            <Input label="Nome completo" value={form.nomeCompleto} onChange={(e) => setForm({ ...form, nomeCompleto: e.target.value })} required />
            <Input label="Login" value={form.login} onChange={(e) => setForm({ ...form, login: e.target.value })} required />
            <Input label="E-mail" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
            <Input label="Senha" type="password" value={form.senha} onChange={(e) => setForm({ ...form, senha: e.target.value })} required />
            <div className="md:col-span-2">
              <span className="block text-sm font-medium text-slate-700 mb-2">Perfis</span>
              <div className="flex flex-wrap gap-3">
                {TODAS_ROLES.map((role) => (
                  <label key={role} className="flex items-center gap-1 text-sm">
                    <input type="checkbox" checked={form.roles.includes(role)} onChange={() => toggleRole(role)} />
                    {ROLE_LABELS[role]}
                  </label>
                ))}
              </div>
            </div>
            <div className="md:col-span-2">
              <Button type="submit">Salvar</Button>
            </div>
          </form>
        </Card>
      )}

      <Card>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b text-left text-slate-500">
              <th className="py-2">Nome</th>
              <th>Login</th>
              <th>Perfis</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {usuarios.map((u) => (
              <tr key={u.id} className="border-b last:border-0">
                <td className="py-2 font-medium text-slate-700">{u.nomeCompleto}</td>
                <td>{u.login}</td>
                <td className="space-x-1">
                  {u.roles.map((r) => (
                    <Badge key={r} color="blue">
                      {ROLE_LABELS[r]}
                    </Badge>
                  ))}
                </td>
                <td>{u.ativo ? <Badge color="green">Ativo</Badge> : <Badge color="red">Inativo</Badge>}</td>
                <td>
                  <Button variant="secondary" className="px-2 py-1 text-xs" onClick={() => alternarStatus(u)}>
                    {u.ativo ? 'Inativar' : 'Ativar'}
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
