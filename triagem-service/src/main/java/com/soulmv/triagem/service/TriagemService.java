package com.soulmv.triagem.service;

import com.soulmv.triagem.client.AtendimentoDto;
import com.soulmv.triagem.dto.request.TriagemRequest;
import com.soulmv.triagem.dto.response.TriagemResponse;
import com.soulmv.triagem.entity.Triagem;
import com.soulmv.triagem.enums.StatusAtendimento;
import com.soulmv.triagem.exception.BusinessException;
import com.soulmv.triagem.exception.ResourceNotFoundException;
import com.soulmv.triagem.mapper.TriagemMapper;
import com.soulmv.triagem.repository.TriagemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Registro da triagem (classificação de risco Manchester) e sua consulta.
 * O atendimento é validado via Feign no atendimento-service; ao registrar a
 * triagem, se o atendimento ainda estava aguardando/em triagem, ele avança pra
 * AGUARDANDO_ATENDIMENTO — mesma regra que existia no monólito, agora como uma
 * segunda chamada Feign em vez de uma escrita local direta.
 */
@Service
public class TriagemService {

    private final TriagemRepository repository;
    private final AtendimentoLookupService atendimentoLookup;
    private final TriagemMapper mapper;

    public TriagemService(TriagemRepository repository,
                          AtendimentoLookupService atendimentoLookup,
                          TriagemMapper mapper) {
        this.repository = repository;
        this.atendimentoLookup = atendimentoLookup;
        this.mapper = mapper;
    }

    @Transactional
    public TriagemResponse registrar(Long atendimentoId, TriagemRequest request, Long enfermeiroId, String enfermeiroNome) {
        AtendimentoDto atendimento = atendimentoLookup.buscar(atendimentoId);
        if (atendimento.status().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível triar.");
        }
        if (repository.existsByAtendimentoId(atendimentoId)) {
            throw new BusinessException("Este atendimento já possui triagem.", HttpStatus.CONFLICT);
        }

        Triagem triagem = Triagem.builder()
                .atendimentoId(atendimentoId)
                .enfermeiroId(enfermeiroId)
                .enfermeiroNome(enfermeiroNome)
                .classificacaoRisco(request.classificacaoRisco())
                .observacao(request.observacao())
                .dataHora(LocalDateTime.now())
                .build();
        triagem = repository.save(triagem);

        if (atendimento.status() == StatusAtendimento.AGUARDANDO_TRIAGEM
                || atendimento.status() == StatusAtendimento.EM_TRIAGEM) {
            atendimentoLookup.atualizarStatus(atendimentoId, StatusAtendimento.AGUARDANDO_ATENDIMENTO);
        }

        return mapper.toResponse(triagem);
    }

    @Transactional(readOnly = true)
    public TriagemResponse buscar(Long atendimentoId) {
        return repository.findByAtendimentoId(atendimentoId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Triagem do atendimento", atendimentoId));
    }
}
