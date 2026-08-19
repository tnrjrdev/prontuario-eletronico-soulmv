package com.soulmv.agendamento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogo-service", path = "/api/setores", configuration = FeignAuthConfig.class)
public interface SetorClient {

    @GetMapping("/{id}")
    SetorDto buscarPorId(@PathVariable("id") Long id);
}
