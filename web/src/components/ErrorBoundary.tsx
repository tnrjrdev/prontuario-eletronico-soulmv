import React from 'react'
import { AlertTriangle, RotateCcw } from 'lucide-react'
import { Button } from './ui'

interface Props {
  children: React.ReactNode
  /** Permite resetar o boundary ao mudar de rota, por exemplo. */
  resetKey?: string
}
interface State {
  error: Error | null
}

/**
 * Captura erros de renderização para que uma falha numa tela não derrube a
 * aplicação inteira. Exibe um estado de erro com opção de tentar novamente.
 */
export class ErrorBoundary extends React.Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: Error): State {
    return { error }
  }

  componentDidUpdate(prev: Props) {
    if (prev.resetKey !== this.props.resetKey && this.state.error) {
      this.setState({ error: null })
    }
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    // Ponto de integração para observabilidade (Sentry/Datadog) em produção.
    console.error('ErrorBoundary capturou um erro:', error, info)
  }

  render() {
    if (this.state.error) {
      return (
        <div className="flex min-h-[50vh] flex-col items-center justify-center p-8 text-center">
          <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-destructive/10 text-destructive">
            <AlertTriangle className="h-7 w-7" />
          </div>
          <h2 className="text-lg font-semibold text-foreground">Algo deu errado nesta tela</h2>
          <p className="mt-1 max-w-md text-sm text-muted-foreground">
            Ocorreu um erro inesperado ao renderizar este conteúdo. Você pode tentar novamente; se persistir, contate o suporte.
          </p>
          <Button className="mt-5" onClick={() => this.setState({ error: null })}>
            <RotateCcw className="mr-2 h-4 w-4" /> Tentar novamente
          </Button>
        </div>
      )
    }
    return this.props.children
  }
}
