package com.soulmv.hospitalar.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração do JWT (prefixo app.security.jwt).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    /** Segredo HMAC (mínimo 32 bytes para HS256). */
    private String secret;

    /** Validade do access token, em minutos. */
    private long accessTokenExpirationMinutes = 60;

    /** Validade do refresh token, em dias. */
    private long refreshTokenExpirationDays = 7;

    /** Emissor (claim iss). */
    private String issuer = "soulmv-hospitalar";
}
