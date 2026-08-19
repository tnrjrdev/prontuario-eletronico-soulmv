package com.soulmv.atendimento.service;

import com.soulmv.atendimento.client.LeitoClient;
import com.soulmv.atendimento.client.LeitoDto;
import com.soulmv.atendimento.client.LeitoStatusRequestDto;
import com.soulmv.atendimento.enums.StatusLeito;
import com.soulmv.atendimento.exception.BusinessException;
import com.soulmv.atendimento.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Muda o status de um leito no catalogo-service via Feign. A precondição de alocação
 * (leito precisa estar LIVRE e ativo) é validada do outro lado, não aqui — ver
 * catalogo-service LeitoService.atualizarStatus. Aqui só traduzimos o retorno em erros
 * que fazem sentido pra quem está internando/liberando um leito.
 */
@Service
public class LeitoLookupService {

    private static final Logger log = LoggerFactory.getLogger(LeitoLookupService.class);

    private final LeitoClient client;

    public LeitoLookupService(LeitoClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "catalogo", fallbackMethod = "fallbackAtualizarStatus")
    public LeitoDto atualizarStatus(Long id, StatusLeito status) {
        try {
            return client.atualizarStatus(id, new LeitoStatusRequestDto(status));
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Leito", id);
        } catch (FeignException.Conflict e) {
            throw new BusinessException("Leito indisponível para esta operação.", HttpStatus.CONFLICT);
        } catch (FeignException.BadRequest e) {
            throw new BusinessException("Leito inválido para esta operação.");
        }
    }

    @SuppressWarnings("unused")
    LeitoDto fallbackAtualizarStatus(Long id, StatusLeito status, Throwable t) {
        log.warn("catalogo-service indisponível ao atualizar status do leito {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Catálogo de leitos indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
