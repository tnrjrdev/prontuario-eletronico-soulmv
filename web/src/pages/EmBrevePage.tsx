import { useLocation } from 'react-router-dom'
import { Construction, Sparkles } from 'lucide-react'
import { PageHeader } from '../components/PageHeader'
import { Card, EmptyState, Badge } from '../components/ui'
import { ROUTE_LABELS } from '../lib/navigation'

export function EmBrevePage() {
  const { pathname } = useLocation()
  const base = '/' + (pathname.split('/')[1] ?? '')
  const label = ROUTE_LABELS[base] ?? 'Módulo'

  return (
    <div>
      <PageHeader
        title={label}
        subtitle="Módulo em desenvolvimento"
        breadcrumbs={[{ label: 'Início', to: '/' }, { label }]}
        actions={<Badge color="info"><Sparkles className="mr-1 h-3 w-3" /> Em breve</Badge>}
      />
      <Card>
        <EmptyState
          icon={<Construction />}
          title={`${label} estará disponível em breve`}
          description="Este módulo faz parte do roadmap do prontuário e está sendo construído. A navegação já reflete a estrutura final do sistema."
        />
      </Card>
    </div>
  )
}
