package com.soulmv.exames.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI hospitalarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SOUL MV Hospitalar — API de Exames")
                        .description("""
                                API REST de solicitação de exames e liberação de laudos.
                                Autentique-se em POST /api/auth/login e use o token
                                retornado no botão 'Authorize' (formato Bearer).""")
                        .version("v0.0.1")
                        .contact(new Contact().name("SOUL MV").email("suporte@soulmv.com"))
                        .license(new License().name("Proprietário")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
