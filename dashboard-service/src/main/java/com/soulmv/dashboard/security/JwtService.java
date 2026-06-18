package com.soulmv.dashboard.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * GeraÃ§Ã£o e validaÃ§Ã£o de tokens JWT (access e refresh) usando HS256.
 */
@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_NOME = "nome";
    private static final String CLAIM_UID = "uid";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }



    public long getAccessTokenExpiracaoSegundos() {
        return properties.getAccessTokenExpirationMinutes() * 60;
    }

    /** Retorna o login (subject) se o token for vÃ¡lido; lanÃ§a JwtException caso contrÃ¡rio. */
    public String extrairLogin(String token) {
        return parse(token).getSubject();
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(parse(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(parse(token).get(CLAIM_TYPE, String.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> extrairRoles(String token) {
        return parse(token).get(CLAIM_ROLES, List.class);
    }

    public boolean isTokenValido(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extrairTodasClaims(String token) {
        return parse(token);
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
