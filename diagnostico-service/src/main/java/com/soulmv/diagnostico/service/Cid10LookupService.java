package com.soulmv.diagnostico.service;

import com.soulmv.diagnostico.client.Cid10Client;
import com.soulmv.diagnostico.client.Cid10Dto;
import com.soulmv.diagnostico.exception.BusinessException;
import com.soulmv.diagnostico.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class Cid10LookupService {

    private static final Logger log = LoggerFactory.getLogger(Cid10LookupService.class);

    private final Cid10Client client;

    public Cid10LookupService(Cid10Client client) {
        this.client = client;
    }

    @CircuitBreaker(name = "catalogo", fallbackMethod = "fallbackBuscar")
    public Cid10Dto buscar(Long id) {
        try {
            return client.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("CID-10", id);
        }
    }

    @SuppressWarnings("unused")
    Cid10Dto fallbackBuscar(Long id, Throwable t) {
        log.warn("catalogo-service indisponível ao buscar CID-10 {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Catálogo de CID-10 indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
