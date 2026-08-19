package com.soulmv.atendimento.service;

import com.soulmv.atendimento.client.SetorClient;
import com.soulmv.atendimento.client.SetorDto;
import com.soulmv.atendimento.exception.BusinessException;
import com.soulmv.atendimento.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SetorLookupService {

    private static final Logger log = LoggerFactory.getLogger(SetorLookupService.class);

    private final SetorClient client;

    public SetorLookupService(SetorClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "catalogo", fallbackMethod = "fallbackBuscar")
    public SetorDto buscar(Long id) {
        try {
            return client.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Setor", id);
        }
    }

    @SuppressWarnings("unused")
    SetorDto fallbackBuscar(Long id, Throwable t) {
        log.warn("catalogo-service indisponível ao buscar setor {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Catálogo de setores indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
