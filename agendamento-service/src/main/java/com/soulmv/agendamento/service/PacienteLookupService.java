package com.soulmv.agendamento.service;

import com.soulmv.agendamento.client.PacienteClient;
import com.soulmv.agendamento.client.PacienteDto;
import com.soulmv.agendamento.exception.BusinessException;
import com.soulmv.agendamento.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Busca um paciente no paciente-service via Feign, protegida por circuit breaker.
 * Caminho de escrita (marcação de agendamento): indisponibilidade do paciente-service
 * vira erro claro, não fallback silencioso. 404 é resultado de negócio normal
 * (pacienteId inválido), não falha do circuito — ver application.yml (ignore-exceptions).
 */
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
