package com.soulmv.anamnese.dto.request;

public record AnamneseRequest(
        String historiaDoencaAtual,
        String antecedentes,
        String alergias,
        String exameFisico
) {
}
