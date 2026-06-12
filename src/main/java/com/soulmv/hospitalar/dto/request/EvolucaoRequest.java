package com.soulmv.hospitalar.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Registro de evolução clínica. O tipo (MEDICA/ENFERMAGEM) é determinado pelo
 * perfil do autor, não pelo cliente.
 */
public record EvolucaoRequest(
        @NotBlank(message = "O texto da evolução é obrigatório")
        String texto
) {
}
