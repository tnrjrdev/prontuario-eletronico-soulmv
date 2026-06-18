package com.soulmv.catalogo.client;

import java.util.Set;

/** Projeção de profissional vinda do iam-service (via Feign). */
public record ProfissionalDto(Long id, String nome, Set<String> roles) {
}
