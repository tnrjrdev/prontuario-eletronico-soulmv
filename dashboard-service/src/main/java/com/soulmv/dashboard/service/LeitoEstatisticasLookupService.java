package com.soulmv.dashboard.service;

import com.soulmv.dashboard.client.LeitoClient;
import com.soulmv.dashboard.client.LeitoEstatisticasDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Busca estatísticas de leitos no catalogo-service via Feign, protegida por circuit
 * breaker. Precisa ser uma classe própria (não um método privado dentro de
 * DashboardService) porque @CircuitBreaker só funciona em chamada externa ao proxy
 * Spring — self-invocation dentro da mesma classe ignora a anotação.
 */
@Service
public class LeitoEstatisticasLookupService {

    private static final Logger log = LoggerFactory.getLogger(LeitoEstatisticasLookupService.class);

    private final LeitoClient leitoClient;

    public LeitoEstatisticasLookupService(LeitoClient leitoClient) {
        this.leitoClient = leitoClient;
    }

    @CircuitBreaker(name = "catalogo", fallbackMethod = "fallbackEstatisticas")
    public LeitoEstatisticasDto estatisticas() {
        return leitoClient.estatisticas();
    }

    @SuppressWarnings("unused")
    LeitoEstatisticasDto fallbackEstatisticas(Throwable t) {
        log.warn("catalogo-service indisponível ao buscar estatísticas de leitos; aplicando fallback. Causa: {}",
                t.toString());
        return new LeitoEstatisticasDto(0, 0, 0, 0, Map.of());
    }
}
