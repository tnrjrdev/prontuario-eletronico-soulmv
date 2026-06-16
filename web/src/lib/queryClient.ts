import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Dados clínicos mudam com frequência moderada; 30s evita refetch agressivo
      // sem deixar a tela defasada por muito tempo.
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})
