package com.soulmv.agendamento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "iam-service", path = "/api/usuarios", configuration = FeignAuthConfig.class)
public interface UsuarioClient {

    @GetMapping("/profissionais")
    List<ProfissionalDto> listarProfissionais();
}
