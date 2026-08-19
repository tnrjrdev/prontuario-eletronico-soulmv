package com.soulmv.evolucao.service;

import com.soulmv.evolucao.client.AtendimentoClient;
import com.soulmv.evolucao.client.AtendimentoDto;
import com.soulmv.evolucao.exception.BusinessException;
import com.soulmv.evolucao.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AtendimentoLookupService {

    private static final Logger log = LoggerFactory.getLogger(AtendimentoLookupService.class);

    private final AtendimentoClient client;

    public AtendimentoLookupService(AtendimentoClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "atendimento", fallbackMethod = "fallbackBuscar")
    public AtendimentoDto buscar(Long id) {
        try {
            return client.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Atendimento", id);
        }
    }

    @SuppressWarnings("unused")
    AtendimentoDto fallbackBuscar(Long id, Throwable t) {
        log.warn("atendimento-service indisponível ao buscar atendimento {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Serviço de atendimentos indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
