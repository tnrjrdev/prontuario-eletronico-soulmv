package com.soulmv.anamnese.service;

import com.soulmv.anamnese.client.AtendimentoDto;
import com.soulmv.anamnese.dto.request.AnamneseRequest;
import com.soulmv.anamnese.dto.response.AnamneseResponse;
import com.soulmv.anamnese.entity.Anamnese;
import com.soulmv.anamnese.exception.BusinessException;
import com.soulmv.anamnese.exception.ResourceNotFoundException;
import com.soulmv.anamnese.mapper.AnamneseMapper;
import com.soulmv.anamnese.repository.AnamneseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AnamneseService {

    private final AnamneseRepository repository;
    private final AtendimentoLookupService atendimentoLookup;
    private final AnamneseMapper mapper;

    public AnamneseService(AnamneseRepository repository,
                           AtendimentoLookupService atendimentoLookup,
                           AnamneseMapper mapper) {
        this.repository = repository;
        this.atendimentoLookup = atendimentoLookup;
        this.mapper = mapper;
    }

    @Transactional
    public AnamneseResponse registrar(Long atendimentoId, AnamneseRequest request, Long medicoId, String medicoNome) {
        AtendimentoDto atendimento = atendimentoLookup.buscar(atendimentoId);
        if (atendimento.status().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível registrar anamnese.");
        }
        if (repository.existsByAtendimentoId(atendimentoId)) {
            throw new BusinessException("Este atendimento já possui anamnese.", HttpStatus.CONFLICT);
        }

        Anamnese anamnese = Anamnese.builder()
                .atendimentoId(atendimentoId)
                .medicoId(medicoId)
                .medicoNome(medicoNome)
                .historiaDoencaAtual(request.historiaDoencaAtual())
                .antecedentes(request.antecedentes())
                .alergias(request.alergias())
                .exameFisico(request.exameFisico())
                .dataHora(LocalDateTime.now())
                .build();
        return mapper.toResponse(repository.save(anamnese));
    }

    @Transactional(readOnly = true)
    public AnamneseResponse buscar(Long atendimentoId) {
        return repository.findByAtendimentoId(atendimentoId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Anamnese do atendimento", atendimentoId));
    }
}
