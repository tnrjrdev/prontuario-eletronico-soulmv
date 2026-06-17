package com.soulmv.iam.dto.response;

import com.soulmv.iam.enums.Role;

import java.util.Set;

/**
 * Projeção enxuta de um profissional de saúde (corpo clínico) para preencher
 * seletores — ex.: escolher o profissional ao agendar. Acessível além do ADMIN.
 */
public record ProfissionalResponse(
        Long id,
        String nome,
        Set<Role> roles
) {
}
