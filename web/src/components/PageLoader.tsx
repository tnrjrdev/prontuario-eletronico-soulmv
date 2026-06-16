import { Spinner } from './ui'

/** Fallback de carregamento para rotas lazy. */
export function PageLoader() {
  return (
    <div className="flex h-[60vh] items-center justify-center" role="status" aria-label="Carregando">
      <Spinner className="h-8 w-8" />
    </div>
  )
}
