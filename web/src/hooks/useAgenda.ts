import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  agendamentoService,
  type AgendamentoFiltro,
  type AgendamentoPayload,
} from '../services/agendamento.service'
import type { StatusAgendamento } from '../types'

export const agendaKeys = {
  lista: (filtro: AgendamentoFiltro) => ['agendamentos', filtro] as const,
  profissionais: () => ['profissionais'] as const,
}

export function useAgendamentos(filtro: AgendamentoFiltro) {
  return useQuery({
    queryKey: agendaKeys.lista(filtro),
    queryFn: () => agendamentoService.listar(filtro),
    refetchInterval: 30_000,
  })
}

export function useProfissionais() {
  return useQuery({
    queryKey: agendaKeys.profissionais(),
    queryFn: () => agendamentoService.profissionais(),
    staleTime: 5 * 60 * 1000,
  })
}

export function useSalvarAgendamento() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id?: number; payload: AgendamentoPayload }) =>
      id ? agendamentoService.atualizar(id, payload) : agendamentoService.criar(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['agendamentos'] }),
  })
}

export function useAtualizarStatusAgendamento() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: StatusAgendamento }) =>
      agendamentoService.atualizarStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['agendamentos'] }),
  })
}

export function useCheckinAgendamento() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => agendamentoService.checkin(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['agendamentos'] })
      qc.invalidateQueries({ queryKey: ['atendimentos'] })
    },
  })
}
