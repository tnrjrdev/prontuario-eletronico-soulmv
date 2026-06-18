package com.soulmv.catalogo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Cliente Feign para o iam-service (resolvido via Eureka pelo nome lógico).
 */
@FeignClient(name = "iam-service", path = "/api/usuarios", configuration = FeignAuthConfig.class)
public interface IamClient {

    @GetMapping("/profissionais")
    List<ProfissionalDto> listarProfissionais();
}
