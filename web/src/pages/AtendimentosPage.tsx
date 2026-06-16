import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { Alert, Badge, Button, Card, Select, type BadgeColor } from '../components/ui'
import { atendimentoService, type AtendimentoPayload } from '../services/atendimento.service'
import { pacienteService } from '../services/paciente.service'
import { catalogoService } from '../services/catalogo.service'
import { extractError } from '../services/api'
import type { Atendimento, Paciente, Setor, StatusAtendimento } from '../types'
import { useAuth } from '../hooks/useAuth'
import { STATUS_ATENDIMENTO_LABELS } from '../utils/constants'
import { formatDate } from '../utils/format'
import { Stethoscope, Plus, ArrowRightCircle, CheckCircle2, Clock, Search, Activity } from 'lucide-react'

const STATUS_CORES: Record<string, BadgeColor> = {
  AGUARDANDO_TRIAGEM: 'attention',
  EM_TRIAGEM: 'orange',
  AGUARDANDO_ATENDIMENTO: 'attention',
  EM_ATENDIMENTO: 'info',
  INTERNADO: 'info',
  AGUARDANDO_EXAME: 'orange',
  ALTA: 'stable',
  CANCELADO: 'critical',
}

const STATUS_EDITAVEIS: StatusAtendimento[] = [
  'EM_TRIAGEM',
  'AGUARDANDO_ATENDIMENTO',
  'EM_ATENDIMENTO',
  'AGUARDANDO_EXAME',
  'CANCELADO',
]

export function AtendimentosPage() {
  const { hasRole } = useAuth()
  const navigate = useNavigate()
  const podeCriar = hasRole('RECEPCAO', 'MEDICO', 'ENFERMEIRO')
  const podeMudarStatus = hasRole('MEDICO', 'ENFERMEIRO')
  const podeAlta = hasRole('MEDICO')

  const [atendimentos, setAtendimentos] = useState<Atendimento[]>([])
  const [pacientes, setPacientes] = useState<Paciente[]>([])
  const [setores, setSetores] = useState<Setor[]>([])
  const [mostrarForm, setMostrarForm] = useState(false)
  const [erro, setErro] = useState('')
  const [form, setForm] = useState<AtendimentoPayload>({ pacienteId: 0, tipo: 'AMBULATORIAL', setorId: 0 })

  const carregar = () => atendimentoService.listar({ size: 50 }).then((p) => setAtendimentos(p.content))

  useEffect(() => {
    carregar()
    pacienteService.listar({ size: 100 }).then((p) => setPacientes(p.content))
    catalogoService.setores().then(setSetores)
  }, [])

  const abrir = async (e: FormEvent) => {
    e.preventDefault()
    setErro('')
    try {
      await atendimentoService.abrir({
        ...form,
        pacienteId: Number(form.pacienteId),
        setorId: Number(form.setorId),
      })
      setMostrarForm(false)
      setForm({ pacienteId: 0, tipo: 'AMBULATORIAL', setorId: 0 })
      carregar()
    } catch (err) {
      setErro(extractError(err))
    }
  }

  const mudarStatus = async (id: number, status: string) => {
    try {
      await atendimentoService.atualizarStatus(id, status)
      carregar()
    } catch (err) {
      alert(extractError(err))
    }
  }

  const darAlta = async (id: number) => {
    if (!confirm('Confirmar alta clínica do paciente?')) return
    try {
      await atendimentoService.darAlta(id)
      carregar()
    } catch (err) {
      alert(extractError(err))
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground">Fila de Atendimento</h2>
          <p className="text-muted-foreground text-sm">Gerencie o fluxo de pacientes na unidade.</p>
        </div>
        {podeCriar && (
          <Button onClick={() => setMostrarForm((v) => !v)} variant={mostrarForm ? "outline" : "primary"}>
            {mostrarForm ? 'Cancelar' : <><Plus className="mr-2 h-4 w-4" /> Abrir Atendimento</>}
          </Button>
        )}
      </div>

      {mostrarForm && (
        <Card className="border-primary/20 bg-primary/5">
          <div className="p-6">
            <h3 className="text-lg font-semibold text-foreground mb-4">Novo Atendimento</h3>
            <form onSubmit={abrir} className="grid grid-cols-1 gap-4 md:grid-cols-12">
              {erro && (
                <div className="md:col-span-12">
                  <Alert variant="destructive">{erro}</Alert>
                </div>
              )}
              <div className="md:col-span-5">
                <Select
                  label="Paciente"
                  value={form.pacienteId || ''}
                  onChange={(e) => setForm({ ...form, pacienteId: Number(e.target.value) })}
                  required
                >
                  <option value="">Selecione pelo nome ou digite...</option>
                  {pacientes.map((p) => (
                    <option key={p.id} value={p.id}>{p.nome}</option>
                  ))}
                </Select>
              </div>
              <div className="md:col-span-3">
                <Select label="Tipo de Atendimento" value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value })}>
                  <option value="AMBULATORIAL">Ambulatorial (Consulta)</option>
                  <option value="EMERGENCIA">Pronto Socorro</option>
                  <option value="INTERNACAO">Internação</option>
                </Select>
              </div>
              <div className="md:col-span-4">
                <Select
                  label="Setor/Especialidade"
                  value={form.setorId || ''}
                  onChange={(e) => setForm({ ...form, setorId: Number(e.target.value) })}
                  required
                >
                  <option value="">Selecione o setor de destino...</option>
                  {setores.map((s) => (
                    <option key={s.id} value={s.id}>{s.nome}</option>
                  ))}
                </Select>
              </div>
              <div className="md:col-span-12 flex justify-end gap-2 mt-2">
                 <Button type="button" variant="ghost" onClick={() => setMostrarForm(false)}>Cancelar</Button>
                 <Button type="submit">Confirmar Abertura</Button>
              </div>
            </form>
          </div>
        </Card>
      )}

      <Card className="flex-1 overflow-hidden">
        <div className="p-4 border-b border-border flex flex-wrap gap-4 items-center justify-between bg-muted/20">
          <div className="flex w-full max-w-md items-center space-x-2">
            <div className="relative w-full">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                className="flex h-9 w-full rounded-md border border-input bg-background pl-9 pr-4 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                placeholder="Filtrar fila..."
              />
            </div>
          </div>
          <div className="flex gap-2 text-sm">
             <Badge color="slate" className="cursor-pointer hover:bg-slate-200">Todos</Badge>
             <Badge color="attention" className="cursor-pointer hover:bg-amber-200">Aguardando</Badge>
             <Badge color="info" className="cursor-pointer hover:bg-blue-200">Em Atendimento</Badge>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-muted/50 border-b border-border">
              <tr className="text-left text-muted-foreground font-medium uppercase text-xs tracking-wider">
                <th className="px-4 py-3 w-16">Senha/ID</th>
                <th className="px-4 py-3">Paciente</th>
                <th className="px-4 py-3">Tipo</th>
                <th className="px-4 py-3">Setor</th>
                <th className="px-4 py-3">Entrada / Espera</th>
                <th className="px-4 py-3">Status Atual</th>
                <th className="px-4 py-3 text-right">Ação Rápida</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {atendimentos.map((a) => (
                <tr key={a.id} className="hover:bg-muted/20 transition-colors group">
                  <td className="px-4 py-3 text-muted-foreground font-mono font-bold">
                    #{String(a.id).padStart(4, '0')}
                  </td>
                  <td className="px-4 py-3 font-semibold text-foreground">
                    {a.pacienteNome}
                  </td>
                  <td className="px-4 py-3 text-muted-foreground">{a.tipo}</td>
                  <td className="px-4 py-3 text-muted-foreground">{a.setorNome}</td>
                  <td className="px-4 py-3 text-muted-foreground flex items-center gap-1">
                    <Clock className="h-3 w-3" />
                    {formatDate(a.dataEntrada).split(' ')[1]} {/* Just showing time to save space */}
                  </td>
                  <td className="px-4 py-3">
                    <Badge color={STATUS_CORES[a.status] ?? 'slate'}>
                      {STATUS_ATENDIMENTO_LABELS[a.status] ?? a.status}
                    </Badge>
                  </td>
                  <td className="px-4 py-3 text-right flex items-center justify-end gap-2">
                    {podeMudarStatus && !['ALTA', 'CANCELADO'].includes(a.status) && (
                      <select
                        className="h-8 rounded-md border border-input bg-background px-2 py-1 text-xs focus:ring-1 focus:ring-ring w-36"
                        value=""
                        onChange={(e) => e.target.value && mudarStatus(a.id, e.target.value)}
                        title="Mudar status operacional"
                      >
                        <option value="" disabled>Status...</option>
                        {STATUS_EDITAVEIS.map((s) => (
                          <option key={s} value={s}>
                            {STATUS_ATENDIMENTO_LABELS[s]}
                          </option>
                        ))}
                      </select>
                    )}
                    {podeAlta && !['ALTA', 'CANCELADO'].includes(a.status) && (
                      <Button variant="outline" size="sm" className="h-8 text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 border-emerald-200" onClick={() => darAlta(a.id)}>
                        <CheckCircle2 className="h-4 w-4 mr-1" />
                        Alta
                      </Button>
                    )}
                    
                    {/* Ações Dinâmicas por Status */}
                    {a.status === 'AGUARDANDO_TRIAGEM' || a.status === 'EM_TRIAGEM' ? (
                      <Button variant="ghost" size="icon" className="h-8 w-8 text-orange-600" title="Realizar Triagem Clínica" onClick={() => navigate(`/triagem/${a.id}`)}>
                        <Activity className="h-5 w-5" />
                      </Button>
                    ) : (
                      podeMudarStatus && !['ALTA', 'CANCELADO'].includes(a.status) && (
                        <Button variant="ghost" size="icon" className="h-8 w-8 text-primary" title="Abrir Prontuário Clínico (PEP)" onClick={() => navigate(`/pep/${a.id}`)}>
                          <ArrowRightCircle className="h-5 w-5" />
                        </Button>
                      )
                    )}
                  </td>
                </tr>
              ))}
              {atendimentos.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-4 py-12 text-center text-muted-foreground">
                    <div className="flex flex-col items-center justify-center">
                      <Stethoscope className="h-8 w-8 mb-2 opacity-20" />
                      <p>A fila de atendimento está vazia.</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  )
}
