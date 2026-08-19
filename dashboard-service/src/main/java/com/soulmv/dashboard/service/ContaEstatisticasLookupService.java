package com.soulmv.dashboard.service;

import com.soulmv.dashboard.client.ContaClient;
import com.soulmv.dashboard.client.ContaEstatisticasDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Busca estatísticas de contas no faturamento-service via Feign, protegida por circuit
 * breaker. Precisa ser uma classe própria pelo mesmo motivo de
 * {@link LeitoEstatisticasLookupService}.
 */
@Service
public class ContaEstatisticasLookupService {

    private static final Logger log = LoggerFactory.getLogger(ContaEstatisticasLookupService.class);

    private final ContaClient contaClient;

    public ContaEstatisticasLookupService(ContaClient contaClient) {
        this.contaClient = contaClient;
    }

    @CircuitBreaker(name = "faturamento", fallbackMethod = "fallbackEstatisticas")
    public ContaEstatisticasDto estatisticas() {
        return contaClient.estatisticas();
    }

    @SuppressWarnings("unused")
    ContaEstatisticasDto fallbackEstatisticas(Throwable t) {
        log.warn("faturamento-service indisponível ao buscar estatísticas de contas; aplicando fallback. Causa: {}",
                t.toString());
        return new ContaEstatisticasDto(0, BigDecimal.ZERO, Map.of(), Map.of());
    }
}
