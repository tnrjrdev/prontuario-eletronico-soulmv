import { Card, Badge, Button } from '../components/ui'
import { FileText, DollarSign, TrendingUp, AlertCircle, FileSpreadsheet } from 'lucide-react'

export function FaturamentoPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground">Faturamento & Faturamento TISS</h2>
          <p className="text-muted-foreground text-sm">Visão geral financeira, envio de lotes TISS e controle de glosas.</p>
        </div>
        <div className="flex gap-2">
           <Button variant="outline"><FileSpreadsheet className="mr-2 h-4 w-4" /> Exportar Relatório</Button>
           <Button variant="primary">Gerar Lote XML TISS</Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="p-4 flex items-center justify-between bg-primary/5 border-primary/20">
           <div>
              <p className="text-sm font-medium text-muted-foreground mb-1">Faturamento Hoje</p>
              <h3 className="text-2xl font-bold text-primary">R$ 142.500</h3>
           </div>
           <div className="p-3 bg-primary/10 rounded-full"><DollarSign className="h-6 w-6 text-primary" /></div>
        </Card>
        <Card className="p-4 flex items-center justify-between">
           <div>
              <p className="text-sm font-medium text-muted-foreground mb-1">Contas Abertas</p>
              <h3 className="text-2xl font-bold text-foreground">84</h3>
           </div>
           <div className="p-3 bg-muted rounded-full"><FileText className="h-6 w-6 text-muted-foreground" /></div>
        </Card>
        <Card className="p-4 flex items-center justify-between">
           <div>
              <p className="text-sm font-medium text-muted-foreground mb-1">Taxa de Glosa Mês</p>
              <h3 className="text-2xl font-bold text-rose-600">3.2%</h3>
           </div>
           <div className="p-3 bg-rose-50 rounded-full"><AlertCircle className="h-6 w-6 text-rose-600" /></div>
        </Card>
        <Card className="p-4 flex items-center justify-between">
           <div>
              <p className="text-sm font-medium text-muted-foreground mb-1">Crescimento YoY</p>
              <h3 className="text-2xl font-bold text-emerald-600">+12%</h3>
           </div>
           <div className="p-3 bg-emerald-50 rounded-full"><TrendingUp className="h-6 w-6 text-emerald-600" /></div>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card title="Geração de Lotes TISS (Pendentes)" className="lg:col-span-2">
           <table className="w-full text-sm">
             <thead className="bg-muted/50 border-b border-border">
               <tr className="text-left text-muted-foreground font-medium">
                 <th className="px-4 py-3">Convênio</th>
                 <th className="px-4 py-3">Guias Fechadas</th>
                 <th className="px-4 py-3">Valor Estimado</th>
                 <th className="px-4 py-3 text-right">Ação</th>
               </tr>
             </thead>
             <tbody className="divide-y divide-border">
               <tr>
                 <td className="px-4 py-3 font-semibold">Unimed Paulista</td>
                 <td className="px-4 py-3">145</td>
                 <td className="px-4 py-3 text-emerald-600 font-medium">R$ 85.000,00</td>
                 <td className="px-4 py-3 text-right"><Button size="sm" variant="outline">Transmitir WS</Button></td>
               </tr>
               <tr>
                 <td className="px-4 py-3 font-semibold">Bradesco Saúde</td>
                 <td className="px-4 py-3">80</td>
                 <td className="px-4 py-3 text-emerald-600 font-medium">R$ 42.300,00</td>
                 <td className="px-4 py-3 text-right"><Button size="sm" variant="outline">Transmitir WS</Button></td>
               </tr>
               <tr>
                 <td className="px-4 py-3 font-semibold">Amil</td>
                 <td className="px-4 py-3">12</td>
                 <td className="px-4 py-3 text-emerald-600 font-medium">R$ 5.400,00</td>
                 <td className="px-4 py-3 text-right"><Button size="sm" variant="outline">Transmitir WS</Button></td>
               </tr>
             </tbody>
           </table>
        </Card>

        <Card title="Contas Hospitalares c/ Pendência">
           <div className="space-y-4 text-sm mt-4">
             <div className="flex items-center justify-between p-3 border border-border rounded-lg bg-rose-50/30">
               <div>
                 <div className="font-semibold text-foreground">Carlos Almeida</div>
                 <div className="text-muted-foreground text-xs">Atend: #0103 - UTI</div>
               </div>
               <Badge color="critical">Falta Assinatura Médica</Badge>
             </div>
             <div className="flex items-center justify-between p-3 border border-border rounded-lg bg-amber-50/30">
               <div>
                 <div className="font-semibold text-foreground">Ana Paula</div>
                 <div className="text-muted-foreground text-xs">Atend: #0104 - Internação</div>
               </div>
               <Badge color="attention">Item S/ Preço no Brasíndice</Badge>
             </div>
           </div>
        </Card>
      </div>
    </div>
  )
}
