import { Suspense, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'
import { ErrorBoundary } from '../components/ErrorBoundary'
import { PageLoader } from '../components/PageLoader'

const COLLAPSE_KEY = 'pep.sidebar.collapsed'

export function AppLayout() {
  const [collapsed, setCollapsed] = useState(() => {
    const saved = localStorage.getItem(COLLAPSE_KEY)
    if (saved === '1') return true
    if (saved === '0') return false
    // Sem preferência salva: recolhe em telas estreitas (tablet/notebook pequeno).
    return typeof window !== 'undefined' && window.innerWidth < 1024
  })

  const toggle = () => {
    setCollapsed((c) => {
      const next = !c
      localStorage.setItem(COLLAPSE_KEY, next ? '1' : '0')
      return next
    })
  }

  const location = useLocation()

  return (
    <div className="flex h-screen bg-background">
      <Sidebar collapsed={collapsed} onToggle={toggle} />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Topbar />
        <main className="flex-1 overflow-auto scrollbar-thin p-8">
          <div className="mx-auto max-w-7xl">
            <ErrorBoundary resetKey={location.pathname}>
              <Suspense fallback={<PageLoader />}>
                <Outlet />
              </Suspense>
            </ErrorBoundary>
          </div>
        </main>
      </div>
    </div>
  )
}
