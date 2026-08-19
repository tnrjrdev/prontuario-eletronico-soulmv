package com.soulmv.triagem.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "atendimento-service", path = "/api/atendimentos", configuration = FeignAuthConfig.class)
public interface AtendimentoClient {

    @GetMapping("/{id}")
    AtendimentoDto buscarPorId(@PathVariable("id") Long id);

    @PatchMapping("/{id}/status")
    AtendimentoDto atualizarStatus(@PathVariable("id") Long id, @RequestBody AtendimentoStatusRequestDto request);
}
