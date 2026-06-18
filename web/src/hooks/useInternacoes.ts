import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { atendimentoService } from '../services/atendimento.service'
import { leitoService } from '../services/leito.service'
import { dashboardService } from '../services/dashboard.service'
import { useAuth } from './useAuth'

const LIVE_REFETCH = 15_000

/** Atendimentos internados (status INTERNADO). */
export function useInternados() {
  return useQuery({
    queryKey: ['atendimentos', { status: 'INTERNADO' }],
    queryFn: () => atendimentoService.listar({ status: 'INTERNADO', size: 200 }),
    refetchInterval: LIVE_REFETCH,
  })
}

/** Internações (tipo INTERNACAO) em aberto — usado para a fila de "aguardando leito". */
export function useInternacoesAbertas() {
  return useQuery({
    queryKey: ['atendimentos', { tipo: 'INTERNACAO' }],
    queryFn: () => atendimentoService.listar({ tipo: 'INTERNACAO', size: 200 }),
    refetchInterval: LIVE_REFETCH,
  })
}

export function useLeitos() {
  return useQuery({
    queryKey: ['leitos'],
    queryFn: () => leitoService.listar(),
    refetchInterval: LIVE_REFETCH,
  })
}

export function useOcupacao() {
  const { hasRole } = useAuth()
  return useQuery({
    queryKey: ['dashboard', 'ocupacao'],
    queryFn: () => dashboardService.ocupacao(),
    enabled: hasRole('ADMIN', 'FATURAMENTO'),
  })
}

export function useAlocarLeito() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ atendimentoId, leitoId }: { atendimentoId: number; leitoId: number }) =>
      atendimentoService.alocarLeito(atendimentoId, leitoId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['atendimentos'] })
      qc.invalidateQueries({ queryKey: ['leitos'] })
    },
  })
}

export function useDarAlta() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (atendimentoId: number) => atendimentoService.darAlta(atendimentoId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['atendimentos'] })
      qc.invalidateQueries({ queryKey: ['leitos'] })
    },
  })
}
