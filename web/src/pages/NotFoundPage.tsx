import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <h1 className="text-4xl font-bold text-slate-700">404</h1>
      <p className="text-slate-500">Página não encontrada.</p>
      <Link to="/" className="mt-4 text-brand-600 hover:underline">
        Voltar ao início
      </Link>
    </div>
  )
}
