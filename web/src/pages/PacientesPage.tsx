import { useEffect, useState, type FormEvent } from 'react'
import { Alert, Badge, Button, Card, Input, Select } from '../components/ui'
import { pacienteService, type PacientePayload } from '../services/paciente.service'
import { catalogoService } from '../services/catalogo.service'
import { extractError } from '../services/api'
import type { Convenio, Paciente } from '../types'
import { formatCpf, formatDateOnly } from '../utils/format'
import { Search, UserPlus, FileText } from 'lucide-react'

const VAZIO: PacientePayload = {
  nome: '',
  cpf: '',
  dataNascimento: '',
  sexo: 'NAO_INFORMADO',
  telefone: '',
  email: '',
  convenioId: null,
  numeroCarteirinha: '',
}

export function PacientesPage() {
  const [pacientes, setPacientes] = useState<Paciente[]>([])
  const [convenios, setConvenios] = useState<Convenio[]>([])
  const [busca, setBusca] = useState('')
  const [form, setForm] = useState<PacientePayload>(VAZIO)
  const [mostrarForm, setMostrarForm] = useState(false)
  const [erro, setErro] = useState('')

  const carregar = () => {
    pacienteService.listar({ nome: busca || undefined, size: 50 }).then((p) => setPacientes(p.content))
  }

  useEffect(() => {
    carregar()
    catalogoService.convenios().then(setConvenios)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const salvar = async (e: FormEvent) => {
    e.preventDefault()
    setErro('')
    try {
      await pacienteService.criar({
        ...form,
        convenioId: form.convenioId ? Number(form.convenioId) : null,
      })
      setForm(VAZIO)
      setMostrarForm(false)
      carregar()
    } catch (err) {
      setErro(extractError(err))
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground">Pacientes</h2>
          <p className="text-muted-foreground text-sm">Gerenciamento do índice mestre de pacientes (MPI).</p>
        </div>
        <Button onClick={() => setMostrarForm((v) => !v)} variant={mostrarForm ? "outline" : "primary"}>
          {mostrarForm ? 'Cancelar' : <><UserPlus className="mr-2 h-4 w-4" /> Novo Paciente</>}
        </Button>
      </div>

      {mostrarForm && (
        <Card className="border-primary/20 bg-primary/5">
          <div className="p-6">
            <h3 className="text-lg font-semibold text-foreground mb-4">Novo Cadastro</h3>
            <form onSubmit={salvar} className="grid grid-cols-1 gap-4 md:grid-cols-12">
              {erro && (
                <div className="md:col-span-12">
                  <Alert variant="destructive">{erro}</Alert>
                </div>
              )}
              
              {/* Row 1 */}
              <div className="md:col-span-6">
                <Input label="Nome Completo" placeholder="Ex: João da Silva" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required />
              </div>
              <div className="md:col-span-3">
                <Input label="CPF" placeholder="000.000.000-00" value={form.cpf} onChange={(e) => setForm({ ...form, cpf: e.target.value })} required />
              </div>
              <div className="md:col-span-3">
                <Input label="Nascimento" type="date" value={form.dataNascimento} onChange={(e) => setForm({ ...form, dataNascimento: e.target.value })} required />
              </div>

              {/* Row 2 */}
              <div className="md:col-span-3">
                <Select label="Sexo Biológico" value={form.sexo} onChange={(e) => setForm({ ...form, sexo: e.target.value })}>
                  <option value="NAO_INFORMADO">Não informado</option>
                  <option value="MASCULINO">Masculino</option>
                  <option value="FEMININO">Feminino</option>
                  <option value="OUTRO">Outro</option>
                </Select>
              </div>
              <div className="md:col-span-3">
                <Input label="Telefone Móvel" placeholder="(00) 00000-0000" value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} />
              </div>
              <div className="md:col-span-6">
                <Input label="E-mail" placeholder="joao@exemplo.com" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
              </div>

              {/* Row 3 */}
              <div className="md:col-span-6">
                <Select label="Convênio / Operadora" value={form.convenioId ?? ''} onChange={(e) => setForm({ ...form, convenioId: e.target.value ? Number(e.target.value) : null })}>
                  <option value="">SUS / Particular (Nenhum)</option>
                  {convenios.map((c) => (
                    <option key={c.id} value={c.id}>{c.nome}</option>
                  ))}
                </Select>
              </div>
              <div className="md:col-span-6">
                <Input label="Nº Carteirinha" placeholder="0000.0000.0000.0000" value={form.numeroCarteirinha} onChange={(e) => setForm({ ...form, numeroCarteirinha: e.target.value })} />
              </div>

              <div className="md:col-span-12 flex justify-end gap-2 mt-2">
                <Button type="button" variant="ghost" onClick={() => setMostrarForm(false)}>Cancelar</Button>
                <Button type="submit">Salvar Paciente</Button>
              </div>
            </form>
          </div>
        </Card>
      )}

      <Card className="flex-1 overflow-hidden">
        <div className="p-4 border-b border-border flex items-center justify-between bg-muted/20">
          <div className="flex w-full max-w-sm items-center space-x-2">
            <div className="relative w-full">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                className="flex h-9 w-full rounded-md border border-input bg-background pl-9 pr-4 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                placeholder="Buscar paciente por nome..."
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && carregar()}
              />
            </div>
            <Button variant="secondary" onClick={carregar} size="sm">Filtrar</Button>
          </div>
          <div className="text-sm text-muted-foreground">{pacientes.length} registros encontrados</div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-muted/50 border-b border-border">
              <tr className="text-left text-muted-foreground font-medium uppercase text-xs tracking-wider">
                <th className="px-4 py-3">Prontuário (ID)</th>
                <th className="px-4 py-3">Paciente</th>
                <th className="px-4 py-3">CPF</th>
                <th className="px-4 py-3">Idade/Nascimento</th>
                <th className="px-4 py-3">Convênio</th>
                <th className="px-4 py-3 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {pacientes.map((p) => (
                <tr key={p.id} className="hover:bg-muted/20 transition-colors group">
                  <td className="px-4 py-3 text-muted-foreground font-mono">{String(p.id).padStart(6, '0')}</td>
                  <td className="px-4 py-3 font-medium text-foreground">{p.nome}</td>
                  <td className="px-4 py-3 text-muted-foreground">{formatCpf(p.cpf)}</td>
                  <td className="px-4 py-3 text-muted-foreground">{formatDateOnly(p.dataNascimento)}</td>
                  <td className="px-4 py-3">
                    {p.convenioNome ? <Badge color="info" className="bg-blue-100 text-blue-700">{p.convenioNome}</Badge> : <Badge variant="outline" className="text-muted-foreground">Particular</Badge>}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <Button variant="ghost" size="sm" className="h-8 text-primary opacity-0 group-hover:opacity-100 transition-opacity">
                      <FileText className="h-4 w-4 mr-2" />
                      Ver PEP
                    </Button>
                  </td>
                </tr>
              ))}
              {pacientes.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-12 text-center text-muted-foreground">
                    <div className="flex flex-col items-center justify-center">
                      <Search className="h-8 w-8 mb-2 opacity-20" />
                      <p>Nenhum paciente encontrado com os filtros atuais.</p>
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
