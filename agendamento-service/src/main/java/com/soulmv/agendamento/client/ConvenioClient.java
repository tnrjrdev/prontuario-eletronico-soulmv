package com.soulmv.agendamento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogo-service", contextId = "convenioClient", path = "/api/convenios", configuration = FeignAuthConfig.class)
public interface ConvenioClient {

    @GetMapping("/{id}")
    ConvenioDto buscarPorId(@PathVariable("id") Long id);
}
