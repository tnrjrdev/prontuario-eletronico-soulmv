package com.soulmv.triagem.service;

import com.soulmv.triagem.client.AtendimentoClient;
import com.soulmv.triagem.client.AtendimentoDto;
import com.soulmv.triagem.client.AtendimentoStatusRequestDto;
import com.soulmv.triagem.enums.StatusAtendimento;
import com.soulmv.triagem.exception.BusinessException;
import com.soulmv.triagem.exception.ResourceNotFoundException;
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

    @CircuitBreaker(name = "atendimento", fallbackMethod = "fallbackAtualizarStatus")
    public void atualizarStatus(Long id, StatusAtendimento status) {
        client.atualizarStatus(id, new AtendimentoStatusRequestDto(status));
    }

    @SuppressWarnings("unused")
    AtendimentoDto fallbackBuscar(Long id, Throwable t) {
        log.warn("atendimento-service indisponível ao buscar atendimento {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Serviço de atendimentos indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @SuppressWarnings("unused")
    void fallbackAtualizarStatus(Long id, StatusAtendimento status, Throwable t) {
        log.warn("atendimento-service indisponível ao atualizar status do atendimento {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Serviço de atendimentos indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
