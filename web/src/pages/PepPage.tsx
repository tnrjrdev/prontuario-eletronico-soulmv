import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Alert, Badge, Button, Card, Input } from '../components/ui'
import { atendimentoService } from '../services/atendimento.service'
import { extractError } from '../services/api'
import type { Atendimento } from '../types'
import { 
  ArrowLeft, Activity, FileText, Pill, FlaskConical, Stethoscope, 
  AlertTriangle, CheckCircle2, History, Info, ChevronRight, MessageSquare
} from 'lucide-react'

export function PepPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [atendimento, setAtendimento] = useState<Atendimento | null>(null)
  const [erro, setErro] = useState('')
  const [activeTab, setActiveTab] = useState<'resumo' | 'evolucao' | 'prescricao' | 'exames'>('resumo')

  useEffect(() => {
    if (id) {
      atendimentoService.listar({ size: 100 })
        .then(res => {
           const found = res.content.find(a => a.id === Number(id))
           if (found) setAtendimento(found)
           else setErro("Atendimento não encontrado.")
        })
        .catch(err => setErro(extractError(err)))
    }
  }, [id])

  if (erro) return <div className="p-8"><Alert variant="destructive">{erro}</Alert><Button className="mt-4" onClick={() => navigate('/atendimentos')}>Voltar</Button></div>
  if (!atendimento) return <div className="p-8 text-center text-muted-foreground">Carregando PEP...</div>

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] -m-8">
      {/* PEP Header Banner */}
      <div className="bg-card border-b border-border p-4 flex items-center justify-between shrink-0 shadow-sm z-10">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate('/atendimentos')} className="text-muted-foreground hover:text-foreground">
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div className="flex flex-col">
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold tracking-tight text-foreground">{atendimento.pacienteNome}</h1>
              <Badge color="blue" className="text-xs">ID: {String(atendimento.pacienteId).padStart(6, '0')}</Badge>
              <Badge color="slate" className="text-xs">Atendimento: #{String(atendimento.id).padStart(4, '0')}</Badge>
            </div>
            <div className="flex gap-4 text-sm text-muted-foreground mt-1 font-medium">
              <span>{atendimento.tipo}</span>
              <span>•</span>
              <span>{atendimento.setorNome}</span>
              <span>•</span>
              <span className="flex items-center gap-1 text-amber-600 dark:text-amber-500"><AlertTriangle className="h-3 w-3" /> Alergia: Penicilina</span>
            </div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <div className="text-right mr-4 hidden md:block">
            <div className="text-sm font-bold text-emerald-600">Sinais Estáveis</div>
            <div className="text-xs text-muted-foreground">Última aferição há 2h</div>
          </div>
          <Button variant="outline" className="border-emerald-200 text-emerald-700 hover:bg-emerald-50">
            <CheckCircle2 className="h-4 w-4 mr-2" />
            Alta Clínica
          </Button>
        </div>
      </div>

      {/* PEP Layout Grid */}
      <div className="flex flex-1 overflow-hidden bg-slate-50/50">
        {/* Left Sidebar - Clinical Context */}
        <div className="w-80 border-r border-border bg-card overflow-y-auto hidden lg:block shrink-0">
          <div className="p-4 space-y-6">
            <div>
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground mb-3 flex items-center gap-2"><Info className="h-4 w-4"/> Dados Básicos</h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between"><span className="text-muted-foreground">Idade:</span><span className="font-medium">45 anos</span></div>
                <div className="flex justify-between"><span className="text-muted-foreground">Sexo:</span><span className="font-medium">Masculino</span></div>
                <div className="flex justify-between"><span className="text-muted-foreground">Sangue:</span><span className="font-medium text-rose-600">O+</span></div>
                <div className="flex justify-between"><span className="text-muted-foreground">Peso:</span><span className="font-medium">78 kg</span></div>
              </div>
            </div>

            <div className="border-t border-border pt-4">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground mb-3 flex items-center gap-2"><AlertTriangle className="h-4 w-4"/> Riscos</h3>
              <div className="flex flex-wrap gap-2">
                <Badge variant="outline" className="border-rose-200 bg-rose-50 text-rose-700">Alergia Grave</Badge>
                <Badge variant="outline" className="border-amber-200 bg-amber-50 text-amber-700">Risco de Queda</Badge>
              </div>
            </div>

            <div className="border-t border-border pt-4">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground mb-3 flex items-center gap-2"><History className="h-4 w-4"/> Diagnósticos Ativos</h3>
              <ul className="space-y-2 text-sm">
                <li className="flex items-start gap-2"><span className="text-primary font-mono text-xs mt-0.5">I10</span> <span className="font-medium text-foreground">Hipertensão essencial</span></li>
                <li className="flex items-start gap-2"><span className="text-primary font-mono text-xs mt-0.5">E11</span> <span className="font-medium text-foreground">Diabetes mellitus</span></li>
              </ul>
            </div>
          </div>
        </div>

        {/* Center - Main Content Area */}
        <div className="flex-1 flex flex-col overflow-hidden bg-background">
          {/* Navigation Tabs */}
          <div className="flex border-b border-border bg-card px-2 shrink-0">
            <button onClick={() => setActiveTab('resumo')} className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors flex items-center gap-2 ${activeTab === 'resumo' ? 'border-primary text-primary' : 'border-transparent text-muted-foreground hover:text-foreground'}`}><Activity className="h-4 w-4" /> Resumo Clínico</button>
            <button onClick={() => setActiveTab('evolucao')} className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors flex items-center gap-2 ${activeTab === 'evolucao' ? 'border-primary text-primary' : 'border-transparent text-muted-foreground hover:text-foreground'}`}><FileText className="h-4 w-4" /> Evoluções</button>
            <button onClick={() => setActiveTab('prescricao')} className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors flex items-center gap-2 ${activeTab === 'prescricao' ? 'border-primary text-primary' : 'border-transparent text-muted-foreground hover:text-foreground'}`}><Pill className="h-4 w-4" /> Prescrições</button>
            <button onClick={() => setActiveTab('exames')} className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors flex items-center gap-2 ${activeTab === 'exames' ? 'border-primary text-primary' : 'border-transparent text-muted-foreground hover:text-foreground'}`}><FlaskConical className="h-4 w-4" /> Exames</button>
          </div>

          {/* Tab Content */}
          <div className="flex-1 overflow-y-auto p-6">
            {activeTab === 'resumo' && (
              <div className="space-y-6 max-w-4xl mx-auto">
                <Card title="Última Evolução (Há 4 horas)">
                  <p className="text-sm text-foreground leading-relaxed whitespace-pre-wrap">
                    Paciente segue estável, eupneico em ar ambiente. Nega dor no momento. 
                    Aceitou bem a dieta oral. Diurese e evacuações presentes.
                    <br/><br/>
                    <strong>Conduta:</strong> Manter prescrição atual. Aguardar exames laboratoriais.
                  </p>
                  <div className="mt-4 pt-4 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
                    <span>Assinado por: Dr. João Silva (CRM-SP 123456)</span>
                    <span>Hoje às 08:30</span>
                  </div>
                </Card>
                
                <div className="grid grid-cols-2 gap-6">
                  <Card title="Sinais Vitais Recentes">
                    <div className="grid grid-cols-2 gap-4 text-sm">
                      <div className="bg-muted/30 p-3 rounded-md border border-border"><div className="text-muted-foreground mb-1 text-xs uppercase font-semibold">Pressão Arterial</div><div className="font-mono text-lg font-bold">120x80 <span className="text-xs font-sans font-normal text-muted-foreground">mmHg</span></div></div>
                      <div className="bg-muted/30 p-3 rounded-md border border-border"><div className="text-muted-foreground mb-1 text-xs uppercase font-semibold">Frequência Cardíaca</div><div className="font-mono text-lg font-bold text-emerald-600">72 <span className="text-xs font-sans font-normal text-muted-foreground">bpm</span></div></div>
                      <div className="bg-muted/30 p-3 rounded-md border border-border"><div className="text-muted-foreground mb-1 text-xs uppercase font-semibold">Saturação (O2)</div><div className="font-mono text-lg font-bold">98 <span className="text-xs font-sans font-normal text-muted-foreground">%</span></div></div>
                      <div className="bg-muted/30 p-3 rounded-md border border-border"><div className="text-muted-foreground mb-1 text-xs uppercase font-semibold">Temperatura</div><div className="font-mono text-lg font-bold">36.5 <span className="text-xs font-sans font-normal text-muted-foreground">°C</span></div></div>
                    </div>
                  </Card>
                  
                  <Card title="Medicamentos Ativos">
                    <ul className="space-y-3 text-sm">
                      <li className="flex items-start justify-between pb-2 border-b border-border last:border-0"><div className="font-medium">Dipirona 1g IV</div><div className="text-muted-foreground">De 6/6h</div></li>
                      <li className="flex items-start justify-between pb-2 border-b border-border last:border-0"><div className="font-medium">Losartana 50mg VO</div><div className="text-muted-foreground">1x ao dia</div></li>
                      <li className="flex items-start justify-between pb-2 border-b border-border last:border-0"><div className="font-medium text-rose-600">Insulina Regular SC</div><div className="text-muted-foreground">Conforme dextro</div></li>
                    </ul>
                  </Card>
                </div>
              </div>
            )}
            
            {activeTab === 'evolucao' && (
              <div className="max-w-4xl mx-auto space-y-4">
                <Card className="border-primary bg-primary/5">
                  <div className="p-4 space-y-4">
                    <h3 className="font-semibold text-primary flex items-center gap-2"><MessageSquare className="h-4 w-4"/> Nova Evolução Clínica</h3>
                    <textarea 
                      className="w-full min-h-[150px] p-3 text-sm rounded-md border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none resize-y" 
                      placeholder="Digite a evolução (suporta atalhos de texto e auto-save)..."
                    />
                    <div className="flex justify-between items-center">
                      <div className="text-xs text-muted-foreground">Auto-salvo há 1 min</div>
                      <Button><CheckCircle2 className="h-4 w-4 mr-2" /> Assinar e Salvar</Button>
                    </div>
                  </div>
                </Card>
                {/* Linha do tempo simulada */}
                <div className="space-y-4 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent before:via-border before:to-transparent mt-8">
                   <div className="relative flex items-center justify-between md:justify-normal md:odd:flex-row-reverse group is-active">
                      <div className="flex items-center justify-center w-10 h-10 rounded-full border-4 border-background bg-emerald-500 text-white shrink-0 md:order-1 md:group-odd:-translate-x-1/2 md:group-even:translate-x-1/2 shadow"><Stethoscope className="h-4 w-4"/></div>
                      <div className="w-[calc(100%-4rem)] md:w-[calc(50%-2.5rem)] p-4 rounded border border-border bg-card shadow-sm">
                         <div className="flex items-center justify-between mb-1"><span className="font-bold text-foreground">Dr. João Silva</span><span className="text-xs font-medium text-emerald-600">Hoje 08:30</span></div>
                         <p className="text-sm text-muted-foreground">Paciente segue estável, eupneico em ar ambiente...</p>
                      </div>
                   </div>
                </div>
              </div>
            )}
            
            {activeTab === 'prescricao' && (
              <div className="max-w-5xl mx-auto space-y-4">
                <div className="flex justify-between items-center mb-6">
                  <h3 className="text-lg font-bold text-foreground">Prescrição Atual</h3>
                  <Button><Plus className="h-4 w-4 mr-2"/> Adicionar Item</Button>
                </div>
                
                <Card className="p-0 overflow-hidden">
                  <table className="w-full text-sm">
                    <thead className="bg-muted/50 border-b border-border">
                      <tr className="text-left text-muted-foreground font-medium uppercase text-xs tracking-wider">
                        <th className="px-4 py-3">Medicamento / Solução</th>
                        <th className="px-4 py-3">Via</th>
                        <th className="px-4 py-3">Posologia</th>
                        <th className="px-4 py-3">Início</th>
                        <th className="px-4 py-3 text-center">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border">
                      <tr className="hover:bg-muted/20">
                        <td className="px-4 py-3 font-medium text-foreground">Dipirona Monoidratada 1g/2mL <br/><span className="text-xs font-normal text-muted-foreground">1 ampola</span></td>
                        <td className="px-4 py-3">IV</td>
                        <td className="px-4 py-3">De 6 em 6 horas (ACM)</td>
                        <td className="px-4 py-3">Hoje 10:00</td>
                        <td className="px-4 py-3 text-center"><Badge color="stable">Ativo</Badge></td>
                      </tr>
                      <tr className="hover:bg-muted/20">
                        <td className="px-4 py-3 font-medium text-foreground">Soro Fisiológico 0.9% 500mL <br/><span className="text-xs font-normal text-muted-foreground">1 frasco</span></td>
                        <td className="px-4 py-3">IV</td>
                        <td className="px-4 py-3">21 gotas/min - Contínuo</td>
                        <td className="px-4 py-3">Ontem 22:00</td>
                        <td className="px-4 py-3 text-center"><Badge color="stable">Ativo</Badge></td>
                      </tr>
                    </tbody>
                  </table>
                  <div className="bg-muted/20 p-4 border-t border-border flex justify-end">
                    <Button variant="outline" className="mr-2">Copiar Prescrição Anterior</Button>
                    <Button variant="primary">Assinar Nova Prescrição</Button>
                  </div>
                </Card>
              </div>
            )}
            
            {activeTab === 'exames' && (
              <div className="max-w-4xl mx-auto text-center py-12 text-muted-foreground">
                <FlaskConical className="h-12 w-12 mx-auto mb-4 opacity-20" />
                <h3 className="text-lg font-semibold text-foreground">Módulo de Exames Interativo</h3>
                <p>O paciente não possui resultados liberados no sistema no plantão atual.</p>
                <Button variant="outline" className="mt-4"><Plus className="mr-2 h-4 w-4"/> Solicitar Exames</Button>
              </div>
            )}
          </div>
        </div>
        
        {/* Right Sidebar - Quick Actions */}
        <div className="w-64 border-l border-border bg-card hidden xl:block shrink-0">
           <div className="p-4 border-b border-border bg-muted/20">
             <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Ações Rápidas</h3>
           </div>
           <div className="p-2 flex flex-col gap-1">
             <button className="flex items-center justify-between w-full p-2 rounded hover:bg-muted text-sm text-foreground font-medium transition-colors">
               <span className="flex items-center gap-2"><FileText className="h-4 w-4 text-primary"/> Emitir Atestado</span>
               <ChevronRight className="h-4 w-4 text-muted-foreground" />
             </button>
             <button className="flex items-center justify-between w-full p-2 rounded hover:bg-muted text-sm text-foreground font-medium transition-colors">
               <span className="flex items-center gap-2"><Pill className="h-4 w-4 text-primary"/> Receituário Especial</span>
               <ChevronRight className="h-4 w-4 text-muted-foreground" />
             </button>
             <button className="flex items-center justify-between w-full p-2 rounded hover:bg-muted text-sm text-foreground font-medium transition-colors">
               <span className="flex items-center gap-2"><Stethoscope className="h-4 w-4 text-primary"/> Escala de Braden</span>
               <ChevronRight className="h-4 w-4 text-muted-foreground" />
             </button>
           </div>
        </div>
      </div>
    </div>
  )
}
