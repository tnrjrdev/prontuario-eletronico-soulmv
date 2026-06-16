import { useState } from 'react'
import { Card, Badge, Button } from '../components/ui'
import { FlaskConical, Search, FileText, CheckCircle2, Clock, Printer, Download, Eye } from 'lucide-react'

const MOCK_EXAMES = [
  { id: 101, paciente: 'João Silva', tipo: 'Hemograma Completo', data: 'Hoje, 08:30', status: 'LIBERADO', prioridade: 'NORMAL' },
  { id: 102, paciente: 'Maria Souza', tipo: 'Raio-X Tórax (PA/Perfil)', data: 'Hoje, 09:15', status: 'LIBERADO', prioridade: 'URGENTE' },
  { id: 103, paciente: 'Carlos Almeida', tipo: 'Tomografia de Crânio', data: 'Hoje, 11:00', status: 'EM_ANALISE', prioridade: 'URGENTE' },
  { id: 104, paciente: 'Ana Paula', tipo: 'Ecocardiograma Transtorácico', data: 'Ontem, 16:45', status: 'LIBERADO', prioridade: 'NORMAL' },
  { id: 105, paciente: 'Roberto Dias', tipo: 'Glicemia de Jejum', data: 'Ontem, 07:00', status: 'COLETADO', prioridade: 'NORMAL' },
]

export function ExamesPage() {
  const [busca, setBusca] = useState('')
  const [selecionado, setSelecionado] = useState<number>(101)

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] -m-8">
      {/* Header Central de Exames */}
      <div className="bg-card border-b border-border p-4 flex items-center justify-between shrink-0 shadow-sm z-10">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <FlaskConical className="h-6 w-6 text-primary" /> Central de Exames
          </h1>
          <p className="text-muted-foreground text-sm">Visualização de resultados, laudos e integrações LIS/PACS.</p>
        </div>
        <div className="flex gap-2">
           <Button variant="outline"><Printer className="h-4 w-4 mr-2" /> Imprimir Lote</Button>
        </div>
      </div>

      <div className="flex flex-1 overflow-hidden bg-background">
        
        {/* Inbox Lateral */}
        <div className="w-96 border-r border-border bg-slate-50/50 flex flex-col shrink-0">
          <div className="p-4 border-b border-border bg-card">
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                className="flex h-9 w-full rounded-md border border-input bg-background pl-9 pr-4 py-2 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                placeholder="Buscar por paciente ou exame..."
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
              />
            </div>
            <div className="flex gap-2 mt-3 overflow-x-auto pb-1 no-scrollbar">
              <Badge color="info" className="cursor-pointer">Todos</Badge>
              <Badge color="stable" className="cursor-pointer opacity-70 hover:opacity-100">Liberados</Badge>
              <Badge color="attention" className="cursor-pointer opacity-70 hover:opacity-100">Urgentes</Badge>
            </div>
          </div>
          
          <div className="flex-1 overflow-y-auto p-2 space-y-2">
            {MOCK_EXAMES.map(exame => (
              <button 
                key={exame.id}
                onClick={() => setSelecionado(exame.id)}
                className={`w-full text-left p-3 rounded-lg border transition-all ${selecionado === exame.id ? 'bg-card border-primary ring-1 ring-primary shadow-sm' : 'bg-transparent border-transparent hover:bg-muted/50'}`}
              >
                <div className="flex justify-between items-start mb-1">
                  <span className="font-semibold text-foreground text-sm">{exame.paciente}</span>
                  {exame.status === 'LIBERADO' && <CheckCircle2 className="h-4 w-4 text-emerald-500" />}
                  {exame.status === 'EM_ANALISE' && <Clock className="h-4 w-4 text-amber-500" />}
                </div>
                <div className="text-sm text-foreground font-medium truncate">{exame.tipo}</div>
                <div className="flex items-center justify-between mt-2">
                  <span className="text-xs text-muted-foreground">{exame.data}</span>
                  {exame.prioridade === 'URGENTE' && <Badge color="critical" className="text-[10px]">URG</Badge>}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Visualizador Principal */}
        <div className="flex-1 flex flex-col bg-slate-100/50 dark:bg-slate-900/50">
          {selecionado === 101 ? (
            <div className="flex-1 p-6 overflow-y-auto">
              <Card className="max-w-4xl mx-auto shadow-md">
                {/* Cabeçalho do Laudo */}
                <div className="border-b border-border pb-6 mb-6">
                  <div className="flex justify-between items-start">
                    <div>
                      <h2 className="text-2xl font-bold text-foreground">Hemograma Completo</h2>
                      <p className="text-muted-foreground mt-1">Coleta: 12/06/2026 08:30 • Liberação: 12/06/2026 10:15</p>
                      <p className="text-sm font-medium mt-2">Paciente: João Silva • Idade: 45 anos</p>
                    </div>
                    <div className="flex gap-2">
                      <Button variant="outline" size="icon"><Download className="h-4 w-4"/></Button>
                      <Button variant="outline" size="icon"><Printer className="h-4 w-4"/></Button>
                    </div>
                  </div>
                </div>

                {/* Corpo do Laudo */}
                <div className="space-y-8">
                  <div>
                    <h3 className="font-bold uppercase tracking-wider text-sm border-b border-border pb-2 mb-4 text-primary">Série Vermelha (Eritrograma)</h3>
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="text-left text-muted-foreground font-medium">
                          <th className="pb-2">Exame</th>
                          <th className="pb-2">Resultado</th>
                          <th className="pb-2">Valores de Referência</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-border">
                        <tr><td className="py-2">Hemácias</td><td className="py-2 font-medium">4,80 milhões/mm³</td><td className="py-2 text-muted-foreground">4,50 a 5,90 milhões/mm³</td></tr>
                        <tr><td className="py-2">Hemoglobina</td><td className="py-2 font-medium">14,2 g/dL</td><td className="py-2 text-muted-foreground">13,5 a 17,5 g/dL</td></tr>
                        <tr><td className="py-2">Hematócrito</td><td className="py-2 font-medium text-rose-600 font-bold">38,5 %</td><td className="py-2 text-muted-foreground">41,0 a 53,0 %</td></tr>
                      </tbody>
                    </table>
                  </div>

                  <div>
                    <h3 className="font-bold uppercase tracking-wider text-sm border-b border-border pb-2 mb-4 text-primary">Série Branca (Leucograma)</h3>
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="text-left text-muted-foreground font-medium">
                          <th className="pb-2">Exame</th>
                          <th className="pb-2">Resultado</th>
                          <th className="pb-2">Valores de Referência</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-border">
                        <tr><td className="py-2">Leucócitos Totais</td><td className="py-2 font-medium">7.500 /mm³</td><td className="py-2 text-muted-foreground">4.500 a 11.000 /mm³</td></tr>
                        <tr><td className="py-2">Neutrófilos</td><td className="py-2 font-medium">60 %</td><td className="py-2 text-muted-foreground">45 a 75 %</td></tr>
                        <tr><td className="py-2">Linfócitos</td><td className="py-2 font-medium text-rose-600 font-bold">45 %</td><td className="py-2 text-muted-foreground">20 a 35 %</td></tr>
                      </tbody>
                    </table>
                  </div>
                </div>

                <div className="mt-8 pt-4 border-t border-border text-sm text-muted-foreground text-center">
                  <p>Laudo assinado eletronicamente por Dra. Marina Costa - CRF-SP 98765</p>
                </div>
              </Card>
            </div>
          ) : selecionado === 102 ? (
             <div className="flex-1 flex flex-col items-center justify-center text-muted-foreground p-6">
                <div className="bg-card p-4 rounded-xl shadow-sm border border-border w-full max-w-4xl aspect-video flex flex-col items-center justify-center relative overflow-hidden">
                   {/* Placeholder para Viewer DICOM */}
                   <div className="absolute inset-0 bg-slate-900 opacity-90 flex items-center justify-center text-white">
                      <div className="text-center">
                        <Eye className="h-16 w-16 mx-auto mb-4 opacity-50" />
                        <h2 className="text-xl font-semibold">Visualizador PACS / DICOM</h2>
                        <p className="opacity-70 mt-2">Imagem de Raio-X Tórax (PA/Perfil) carregada.</p>
                      </div>
                   </div>
                </div>
                <div className="mt-6 text-center max-w-2xl bg-card p-4 rounded-lg border border-border">
                  <h3 className="font-bold text-foreground">Laudo Radiológico</h3>
                  <p className="mt-2 text-sm text-foreground">Aumento da área cardíaca. Sinais de congestão pulmonar peri-hilar. Seios costofrênicos livres. Estruturas ósseas preservadas.</p>
                </div>
             </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-muted-foreground">
              <FileText className="h-16 w-16 mb-4 opacity-20" />
              <h2 className="text-xl font-semibold text-foreground">Resultado em Processamento</h2>
              <p>O laudo ou imagem deste exame ainda não está disponível.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
