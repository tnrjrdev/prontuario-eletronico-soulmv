package com.soulmv.agendamento.client;

import java.util.Set;

/** Espelha iam-service ProfissionalResponse. Já vem filtrada (ativos com role clínica). */
public record ProfissionalDto(Long id, String nome, Set<String> roles) {
}
