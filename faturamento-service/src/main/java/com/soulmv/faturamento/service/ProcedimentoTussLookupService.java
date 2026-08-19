package com.soulmv.faturamento.service;

import com.soulmv.faturamento.client.ProcedimentoTussClient;
import com.soulmv.faturamento.client.ProcedimentoTussDto;
import com.soulmv.faturamento.exception.BusinessException;
import com.soulmv.faturamento.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Busca um procedimento TUSS no catalogo-service via Feign, protegida por circuit
 * breaker. Diferente dos lookups de estatística do dashboard-service, isso está no
 * caminho de escrita (fecha valor de um item de conta): não é seguro degradar para
 * um valor inventado, então a falha do catálogo aqui vira um erro claro para quem
 * está faturando, não um fallback silencioso. Um procedimento inexistente (404) é
 * tratado como resultado de negócio normal, não como falha do circuito — configurado
 * em application.yml (resilience4j.circuitbreaker.instances.catalogo.ignore-exceptions).
 */
@Service
public class ProcedimentoTussLookupService {

    private static final Logger log = LoggerFactory.getLogger(ProcedimentoTussLookupService.class);

    private final ProcedimentoTussClient client;

    public ProcedimentoTussLookupService(ProcedimentoTussClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "catalogo", fallbackMethod = "fallbackBuscar")
    public ProcedimentoTussDto buscar(Long id) {
        try {
            return client.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Procedimento TUSS", id);
        }
    }

    @SuppressWarnings("unused")
    ProcedimentoTussDto fallbackBuscar(Long id, Throwable t) {
        log.warn("catalogo-service indisponível ao buscar procedimento TUSS {}; recusando a operação. Causa: {}",
                id, t.toString());
        throw new BusinessException(
                "Catálogo de procedimentos indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
