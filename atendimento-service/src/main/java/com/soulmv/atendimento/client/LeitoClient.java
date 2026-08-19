package com.soulmv.atendimento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "catalogo-service", contextId = "leitoClient", path = "/api/leitos", configuration = FeignAuthConfig.class)
public interface LeitoClient {

    /**
     * A precondição (leito precisa estar LIVRE e ativo pra virar OCUPADO) é validada
     * no catalogo-service, dono do dado — evita condição de corrida entre dois
     * atendimentos tentando alocar o mesmo leito ao mesmo tempo.
     */
    @PatchMapping("/{id}/status")
    LeitoDto atualizarStatus(@PathVariable("id") Long id, @RequestBody LeitoStatusRequestDto request);
}
