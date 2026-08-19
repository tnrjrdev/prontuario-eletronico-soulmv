package com.soulmv.agendamento.service;

import com.soulmv.agendamento.client.ConvenioClient;
import com.soulmv.agendamento.client.ConvenioDto;
import com.soulmv.agendamento.exception.BusinessException;
import com.soulmv.agendamento.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ConvenioLookupService {

    private static final Logger log = LoggerFactory.getLogger(ConvenioLookupService.class);

    private final ConvenioClient client;

    public ConvenioLookupService(ConvenioClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "catalogo", fallbackMethod = "fallbackBuscar")
    public ConvenioDto buscar(Long id) {
        try {
            return client.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Convênio", id);
        }
    }

    @SuppressWarnings("unused")
    ConvenioDto fallbackBuscar(Long id, Throwable t) {
        log.warn("catalogo-service indisponível ao buscar convênio {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Catálogo de convênios indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
