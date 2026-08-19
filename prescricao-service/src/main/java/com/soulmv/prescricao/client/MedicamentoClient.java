package com.soulmv.prescricao.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogo-service", path = "/api/medicamentos", configuration = FeignAuthConfig.class)
public interface MedicamentoClient {

    @GetMapping("/{id}")
    MedicamentoDto buscarPorId(@PathVariable("id") Long id);
}
