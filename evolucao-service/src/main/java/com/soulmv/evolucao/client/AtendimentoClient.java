package com.soulmv.evolucao.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "atendimento-service", path = "/api/atendimentos", configuration = FeignAuthConfig.class)
public interface AtendimentoClient {

    @GetMapping("/{id}")
    AtendimentoDto buscarPorId(@PathVariable("id") Long id);
}
