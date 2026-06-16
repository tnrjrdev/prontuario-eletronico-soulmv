import { useEffect, useState } from 'react'
import { Alert, Badge, Button, Card, Select } from '../components/ui'
import { atendimentoService } from '../services/atendimento.service'
import { extractError } from '../services/api'
import type { Atendimento } from '../types'
import { Clock, Search, CheckSquare, XSquare, AlertCircle, Pill, Syringe, Activity } from 'lucide-react'

// Mock data para horários de medicação (idealmente viria do backend via ItensPrescricao)
const MOCK_PRESCRICOES = [
  { id: 1, medicamento: 'Dipirona Monoidratada 1g/2mL IV', horario: '08:00', status: 'CHECADO', resp: 'Enf. Carlos' },
  { id: 2, medicamento: 'Omeprazol 40mg IV', horario: '08:00', status: 'CHECADO', resp: 'Enf. Carlos' },
  { id: 3, medicamento: 'Insulina Regular SC', horario: '12:00', status: 'PENDENTE', resp: null, alerta: 'Aguardando Dextro' },
  { id: 4, medicamento: 'Ceftriaxona 1g IV', horario: '12:00', status: 'PENDENTE', resp: null },
  { id: 5, medicamento: 'Furosemida 20mg IV', horario: '14:00', status: 'ATRASADO', resp: null },
]

export function EnfermagemPage() {
  const [atendimentos, setAtendimentos] = useState<Atendimento[]>([])
  const [erro, setErro] = useState('')
  const [busca, setBusca] = useState('')
  const [pacienteSelecionado, setPacienteSelecionado] = useState<number | null>(null)

  const carregar = () => atendimentoService.listar({ size: 100 }).then((p) => {
    // Filtrar apenas pacientes que necessitam de cuidados de enfermagem (Ex: INTERNADO, EM_ATENDIMENTO, AGUARDANDO_EXAME)
    const emCuidado = p.content.filter(a => ['INTERNADO', 'EM_ATENDIMENTO', 'AGUARDANDO_EXAME'].includes(a.status))
    setAtendimentos(emCuidado)
  }).catch(err => setErro(extractError(err)))

  useEffect(() => {
    carregar()
  }, [])

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] -m-8">
      {/* Header Beira-Leito */}
      <div className="bg-card border-b border-border p-4 flex items-center justify-between shrink-0 shadow-sm z-10">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <Activity className="h-6 w-6 text-primary" /> Painel de Enfermagem
          </h1>
          <p className="text-muted-foreground text-sm">Gestão de checagem beira-leito e administração de medicamentos.</p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex gap-2">
            <Badge color="stable" className="text-xs">8 Checagens Feitas</Badge>
            <Badge color="critical" className="text-xs">2 Atrasadas</Badge>
          </div>
          <Select value="plantao_diurno" onChange={() => {}} className="w-48 h-9">
             <option value="plantao_diurno">Plantão: 07h - 19h</option>
             <option value="plantao_noturno">Plantão: 19h - 07h</option>
          </Select>
        </div>
      </div>

      {erro && <div className="p-4"><Alert variant="destructive">{erro}</Alert></div>}

      <div className="flex flex-1 overflow-hidden bg-slate-50/50">
        
        {/* Coluna Esquerda: Lista de Pacientes no Setor */}
        <div className="w-96 border-r border-border bg-card flex flex-col shrink-0">
          <div className="p-4 border-b border-border bg-muted/20">
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                className="flex h-9 w-full rounded-md border border-input bg-background pl-9 pr-4 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                placeholder="Buscar paciente ou leito..."
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
              />
            </div>
          </div>
          <div className="flex-1 overflow-y-auto p-2 space-y-2">
             {atendimentos.filter(a => a.pacienteNome.toLowerCase().includes(busca.toLowerCase())).map(a => (
               <button 
                 key={a.id}
                 onClick={() => setPacienteSelecionado(a.id)}
                 className={`w-full text-left p-3 rounded-lg border transition-all ${pacienteSelecionado === a.id ? 'bg-primary/5 border-primary ring-1 ring-primary' : 'bg-background border-border hover:border-primary/50'}`}
               >
                 <div className="flex justify-between items-start mb-1">
                   <span className="font-semibold text-foreground text-sm truncate pr-2">{a.pacienteNome}</span>
                   <Badge color={a.status === 'INTERNADO' ? 'info' : 'slate'} className="text-[10px] shrink-0">{a.setorNome.split(' ')[0]}</Badge>
                 </div>
                 <div className="text-xs text-muted-foreground flex items-center justify-between">
                   <span>Atend: #{a.id}</span>
                   <span className="flex items-center gap-1 text-rose-600 font-medium"><Clock className="h-3 w-3"/> 1 Atraso</span>
                 </div>
               </button>
             ))}
             {atendimentos.length === 0 && <div className="p-4 text-center text-sm text-muted-foreground">Nenhum paciente ativo no momento.</div>}
          </div>
        </div>

        {/* Coluna Central: Painel do Paciente Selecionado */}
        <div className="flex-1 overflow-y-auto bg-background p-6">
          {!pacienteSelecionado ? (
             <div className="h-full flex flex-col items-center justify-center text-muted-foreground">
                <Syringe className="h-16 w-16 mb-4 opacity-20" />
                <h2 className="text-xl font-semibold text-foreground">Nenhum Paciente Selecionado</h2>
                <p>Selecione um paciente na lista lateral para visualizar a prescrição e realizar checagens.</p>
             </div>
          ) : (
            <div className="max-w-4xl mx-auto space-y-6">
               
               {/* Resumo do Paciente Selecionado */}
               {(() => {
                 const pac = atendimentos.find(a => a.id === pacienteSelecionado)
                 if (!pac) return null;
                 return (
                   <Card className="border-l-4 border-l-primary bg-primary/5 shadow-sm">
                     <div className="p-4 flex items-center justify-between">
                       <div>
                         <h2 className="text-xl font-bold text-foreground">{pac.pacienteNome}</h2>
                         <p className="text-sm text-muted-foreground mt-1">ID: {pac.pacienteId} • Setor: {pac.setorNome} • Peso: 78kg • Alergias: <span className="text-rose-600 font-semibold">Nenhuma conhecida</span></p>
                       </div>
                       <Button variant="outline" size="sm"><Activity className="mr-2 h-4 w-4"/> Anotar Sinais Vitais</Button>
                     </div>
                   </Card>
                 )
               })()}

               <div className="flex items-center justify-between border-b border-border pb-2">
                 <h3 className="text-lg font-semibold text-foreground flex items-center gap-2"><Pill className="h-5 w-5"/> Cronograma de Medicação</h3>
                 <div className="flex gap-2">
                   <Button variant="outline" size="sm">Histórico de Checagem</Button>
                   <Button variant="primary" size="sm">Checar Múltiplos</Button>
                 </div>
               </div>

               {/* Tabela de Checagem */}
               <Card className="p-0 overflow-hidden shadow-sm">
                 <table className="w-full text-sm">
                   <thead className="bg-muted/50 border-b border-border">
                     <tr className="text-left text-muted-foreground font-medium uppercase text-xs tracking-wider">
                       <th className="px-4 py-3 w-20">Horário</th>
                       <th className="px-4 py-3">Prescrição</th>
                       <th className="px-4 py-3 text-center">Status</th>
                       <th className="px-4 py-3 text-right">Ações de Checagem</th>
                     </tr>
                   </thead>
                   <tbody className="divide-y divide-border">
                     {MOCK_PRESCRICOES.map(item => (
                       <tr key={item.id} className={`hover:bg-muted/20 transition-colors ${item.status === 'ATRASADO' ? 'bg-rose-50/50' : ''}`}>
                         <td className="px-4 py-4 font-mono font-bold text-foreground">
                           <div className="flex items-center gap-1">
                             <Clock className={`h-4 w-4 ${item.status === 'ATRASADO' ? 'text-rose-500' : 'text-muted-foreground'}`} />
                             {item.horario}
                           </div>
                         </td>
                         <td className="px-4 py-4">
                           <div className="font-semibold text-foreground">{item.medicamento}</div>
                           {item.alerta && <div className="text-xs text-amber-600 flex items-center gap-1 mt-1"><AlertCircle className="h-3 w-3"/> {item.alerta}</div>}
                         </td>
                         <td className="px-4 py-4 text-center">
                           {item.status === 'CHECADO' && <Badge color="stable">Checado ({item.resp})</Badge>}
                           {item.status === 'PENDENTE' && <Badge color="slate">Pendente</Badge>}
                           {item.status === 'ATRASADO' && <Badge color="critical">Atrasado</Badge>}
                         </td>
                         <td className="px-4 py-4 text-right">
                           {item.status !== 'CHECADO' ? (
                             <div className="flex items-center justify-end gap-2">
                               <Button variant="outline" size="sm" className="h-8 border-rose-200 text-rose-700 hover:bg-rose-50" title="Recusar / Não Administrado">
                                 <XSquare className="h-4 w-4" />
                               </Button>
                               <Button variant="outline" size="sm" className="h-8 border-emerald-200 text-emerald-700 hover:bg-emerald-50" title="Confirmar Administração">
                                 <CheckSquare className="h-4 w-4 mr-1" /> Checar
                               </Button>
                             </div>
                           ) : (
                             <span className="text-xs text-muted-foreground">Adminstrado</span>
                           )}
                         </td>
                       </tr>
                     ))}
                   </tbody>
                 </table>
               </Card>

            </div>
          )}
        </div>
      </div>
    </div>
  )
}
