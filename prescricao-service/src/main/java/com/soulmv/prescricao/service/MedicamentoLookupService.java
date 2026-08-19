package com.soulmv.prescricao.service;

import com.soulmv.prescricao.client.MedicamentoClient;
import com.soulmv.prescricao.client.MedicamentoDto;
import com.soulmv.prescricao.exception.BusinessException;
import com.soulmv.prescricao.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MedicamentoLookupService {

    private static final Logger log = LoggerFactory.getLogger(MedicamentoLookupService.class);

    private final MedicamentoClient client;

    public MedicamentoLookupService(MedicamentoClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "catalogo", fallbackMethod = "fallbackBuscar")
    public MedicamentoDto buscar(Long id) {
        try {
            return client.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Medicamento", id);
        }
    }

    @SuppressWarnings("unused")
    MedicamentoDto fallbackBuscar(Long id, Throwable t) {
        log.warn("catalogo-service indisponível ao buscar medicamento {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Serviço de catálogo indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
