package com.soulmv.prescricao.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    private String secret;

    private long accessTokenExpirationMinutes = 60;

    private long refreshTokenExpirationDays = 7;

    private String issuer = "soulmv-hospitalar";
}
