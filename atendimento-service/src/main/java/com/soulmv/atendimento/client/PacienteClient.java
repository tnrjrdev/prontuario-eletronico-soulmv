package com.soulmv.atendimento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "paciente-service", path = "/api/pacientes", configuration = FeignAuthConfig.class)
public interface PacienteClient {

    @GetMapping("/{id}")
    PacienteDto buscarPorId(@PathVariable("id") Long id);
}
