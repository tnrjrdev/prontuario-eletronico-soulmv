package com.soulmv.hospitalar.security;

import com.soulmv.hospitalar.entity.Usuario;
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
 * Geração e validação de tokens JWT (access e refresh) usando HS256.
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

    public String gerarAccessToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant exp = agora.plus(properties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(usuario.getLogin())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_UID, usuario.getId())
                .claim(CLAIM_NOME, usuario.getNomeCompleto())
                .claim(CLAIM_ROLES, usuario.getRoles().stream().map(Enum::name).toList())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String gerarRefreshToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant exp = agora.plus(properties.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(usuario.getLogin())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim(CLAIM_UID, usuario.getId())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public long getAccessTokenExpiracaoSegundos() {
        return properties.getAccessTokenExpirationMinutes() * 60;
    }

    /** Retorna o login (subject) se o token for válido; lança JwtException caso contrário. */
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

    public boolean isValido(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
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
