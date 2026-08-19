package com.soulmv.auditoria.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Protege o endpoint interno de escrita da auditoria (POST /api/auditoria/eventos).
 * Esse endpoint não tem usuário logado por trás (é chamado pelo api-gateway para
 * TODA requisição, inclusive as que falharam autenticação), então em vez de JWT
 * ele exige um segredo compartilhado só entre gateway e este serviço.
 */
@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Token";
    private static final String PATH = "/api/auditoria/eventos";

    private final String tokenEsperado;

    public InternalTokenFilter(@Value("${app.security.internal-audit-token}") String tokenEsperado) {
        this.tokenEsperado = tokenEsperado;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (PATH.equals(request.getRequestURI())) {
            String recebido = request.getHeader(HEADER);
            if (recebido == null || !MessageDigest.isEqual(
                    recebido.getBytes(StandardCharsets.UTF_8), tokenEsperado.getBytes(StandardCharsets.UTF_8))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token interno inválido ou ausente.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
