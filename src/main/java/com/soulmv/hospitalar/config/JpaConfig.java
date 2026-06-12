package com.soulmv.hospitalar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita a auditoria do JPA (preenchimento de criadoEm/atualizadoEm em BaseEntity).
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
