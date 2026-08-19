package com.soulmv.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "catalogo-service", path = "/api/leitos", configuration = FeignAuthConfig.class)
public interface LeitoClient {

    @GetMapping("/estatisticas")
    LeitoEstatisticasDto estatisticas();
}
