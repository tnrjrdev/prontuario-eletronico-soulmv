package com.soulmv.faturamento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogo-service", path = "/api/procedimentos-tuss", configuration = FeignAuthConfig.class)
public interface ProcedimentoTussClient {

    @GetMapping("/{id}")
    ProcedimentoTussDto buscarPorId(@PathVariable("id") Long id);
}
