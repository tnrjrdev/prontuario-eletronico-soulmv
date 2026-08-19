package com.soulmv.diagnostico.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogo-service", path = "/api/cid10", configuration = FeignAuthConfig.class)
public interface Cid10Client {

    @GetMapping("/{id}")
    Cid10Dto buscarPorId(@PathVariable("id") Long id);
}
