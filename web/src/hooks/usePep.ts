import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { atendimentoService } from '../services/atendimento.service'
import { clinicoService } from '../services/clinico.service'
import { pacienteService } from '../services/paciente.service'

/**
 * Query keys centralizadas do prontuário, para invalidação consistente.
 */
export const pepKeys = {
  atendimento: (id: number) => ['atendimento', id] as const,
  paciente: (id: number) => ['paciente', id] as const,
  anamnese: (id: number) => ['anamnese', id] as const,
  diagnosticos: (id: number) => ['diagnosticos', id] as const,
  sinaisVitais: (id: number) => ['sinais-vitais', id] as const,
  evolucoes: (id: number) => ['evolucoes', id] as const,
  prescricoes: (id: number) => ['prescricoes', id] as const,
  exames: (id: number) => ['exames', id] as const,
}

export function useAtendimento(id: number) {
  return useQuery({
    queryKey: pepKeys.atendimento(id),
    queryFn: () => atendimentoService.buscar(id),
    enabled: Number.isFinite(id) && id > 0,
  })
}

export function usePaciente(id: number | undefined) {
  return useQuery({
    queryKey: pepKeys.paciente(id ?? 0),
    queryFn: () => pacienteService.buscar(id as number),
    enabled: !!id && id > 0,
  })
}

export function useAnamnese(atendimentoId: number) {
  return useQuery({
    queryKey: pepKeys.anamnese(atendimentoId),
    queryFn: () => clinicoService.anamnese(atendimentoId),
    enabled: atendimentoId > 0,
  })
}

export function useDiagnosticos(atendimentoId: number) {
  return useQuery({
    queryKey: pepKeys.diagnosticos(atendimentoId),
    queryFn: () => clinicoService.diagnosticos(atendimentoId),
    enabled: atendimentoId > 0,
  })
}

export function useSinaisVitais(atendimentoId: number) {
  return useQuery({
    queryKey: pepKeys.sinaisVitais(atendimentoId),
    queryFn: () => clinicoService.sinaisVitais(atendimentoId),
    enabled: atendimentoId > 0,
  })
}

export function useEvolucoes(atendimentoId: number) {
  return useQuery({
    queryKey: pepKeys.evolucoes(atendimentoId),
    queryFn: () => clinicoService.evolucoes(atendimentoId),
    enabled: atendimentoId > 0,
  })
}

export function usePrescricoes(atendimentoId: number) {
  return useQuery({
    queryKey: pepKeys.prescricoes(atendimentoId),
    queryFn: () => clinicoService.prescricoes(atendimentoId),
    enabled: atendimentoId > 0,
  })
}

export function useExames(atendimentoId: number) {
  return useQuery({
    queryKey: pepKeys.exames(atendimentoId),
    queryFn: () => clinicoService.exames(atendimentoId),
    enabled: atendimentoId > 0,
  })
}

export function useRegistrarEvolucao(atendimentoId: number) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (texto: string) => clinicoService.registrarEvolucao(atendimentoId, texto),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: pepKeys.evolucoes(atendimentoId) })
    },
  })
}
