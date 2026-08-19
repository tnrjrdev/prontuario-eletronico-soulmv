package com.soulmv.agendamento.service;

import com.soulmv.agendamento.client.ProfissionalDto;
import com.soulmv.agendamento.client.UsuarioClient;
import com.soulmv.agendamento.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Valida um profissional para agendamento chamando GET /api/usuarios/profissionais no
 * iam-service — o mesmo endpoint que o catalogo-service já consome, que devolve só
 * usuários ATIVOS com role clínica (MEDICO/ENFERMEIRO). Estar na lista já responde
 * "existe + está ativo + é profissional de saúde" numa única chamada, delegando a
 * regra para quem é dono do dado (iam-service) em vez de reimplementá-la aqui contra
 * uma cópia potencialmente desatualizada.
 */
@Service
public class ProfissionalValidationService {

    private static final Logger log = LoggerFactory.getLogger(ProfissionalValidationService.class);

    private final UsuarioClient client;

    public ProfissionalValidationService(UsuarioClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "iam", fallbackMethod = "fallbackValidar")
    public ProfissionalDto validar(Long profissionalId) {
        return client.listarProfissionais().stream()
                .filter(p -> p.id().equals(profissionalId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Profissional não encontrado, inativo, ou sem perfil clínico (médico/enfermeiro)."));
    }

    @SuppressWarnings("unused")
    ProfissionalDto fallbackValidar(Long profissionalId, Throwable t) {
        log.warn("iam-service indisponível ao validar profissional {}; recusando a operação. Causa: {}",
                profissionalId, t.toString());
        throw new BusinessException(
                "Serviço de profissionais indisponível no momento. Tente novamente em instantes.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }
}
