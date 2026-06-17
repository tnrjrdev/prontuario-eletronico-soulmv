package com.soulmv.iam.dto.response;

/**
 * Resposta de autenticaÃ§Ã£o: tokens emitidos + dados bÃ¡sicos do usuÃ¡rio logado.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UsuarioResponse usuario
) {
    public static TokenResponse bearer(String accessToken, String refreshToken,
                                       long expiresIn, UsuarioResponse usuario) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn, usuario);
    }
}
