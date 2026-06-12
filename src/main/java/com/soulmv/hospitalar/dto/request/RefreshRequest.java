package com.soulmv.hospitalar.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicitação de renovação de access token a partir de um refresh token válido.
 */
public record RefreshRequest(
        @NotBlank(message = "O refreshToken é obrigatório")
        String refreshToken
) {
}
