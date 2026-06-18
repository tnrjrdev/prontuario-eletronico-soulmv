package com.soulmv.catalogo.service;

import com.soulmv.catalogo.client.IamClient;
import com.soulmv.catalogo.client.ProfissionalDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Consulta profissionais no iam-service via Feign, protegida por circuit breaker
 * (Resilience4j). Em caso de indisponibilidade/timeout, devolve lista vazia
 * (degradação graciosa) em vez de propagar a falha.
 */
@Service
public class ProfissionalLookupService {

    private static final Logger log = LoggerFactory.getLogger(ProfissionalLookupService.class);

    private final IamClient iamClient;

    public ProfissionalLookupService(IamClient iamClient) {
        this.iamClient = iamClient;
    }

    @CircuitBreaker(name = "iam", fallbackMethod = "fallbackProfissionais")
    public List<ProfissionalDto> listarProfissionais() {
        return iamClient.listarProfissionais();
    }

    @SuppressWarnings("unused")
    private List<ProfissionalDto> fallbackProfissionais(Throwable t) {
        log.warn("iam-service indisponível ao listar profissionais; aplicando fallback. Causa: {}", t.toString());
        return List.of();
    }
}
