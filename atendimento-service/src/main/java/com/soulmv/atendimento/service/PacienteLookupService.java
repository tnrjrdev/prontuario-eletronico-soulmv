package com.soulmv.atendimento.service;

import com.soulmv.atendimento.client.PacienteClient;
import com.soulmv.atendimento.client.PacienteDto;
import com.soulmv.atendimento.exception.BusinessException;
import com.soulmv.atendimento.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PacienteLookupService {

    private static final Logger log = LoggerFactory.getLogger(PacienteLookupService.class);

    private final PacienteClient client;

    public PacienteLookupService(PacienteClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "paciente", fallbackMethod = "fallbackBuscar")
    public PacienteDto buscar(Long id) {
        try {
            return client.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Paciente", id);
        }
    }

    @SuppressWarnings("unused")
    PacienteDto fallbackBuscar(Long id, Throwable t) {
        log.warn("paciente-service indisponível ao buscar paciente {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Serviço de pacientes indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
