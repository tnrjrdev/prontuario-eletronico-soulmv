package com.soulmv.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "faturamento-service", path = "/api/contas", configuration = FeignAuthConfig.class)
public interface ContaClient {

    @GetMapping("/estatisticas")
    ContaEstatisticasDto estatisticas();
}
