package com.soulmv.diagnostico.service;

import com.soulmv.diagnostico.client.AtendimentoDto;
import com.soulmv.diagnostico.client.Cid10Dto;
import com.soulmv.diagnostico.dto.request.DiagnosticoRequest;
import com.soulmv.diagnostico.dto.response.DiagnosticoResponse;
import com.soulmv.diagnostico.entity.Diagnostico;
import com.soulmv.diagnostico.exception.BusinessException;
import com.soulmv.diagnostico.mapper.DiagnosticoMapper;
import com.soulmv.diagnostico.repository.DiagnosticoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository repository;
    private final AtendimentoLookupService atendimentoLookup;
    private final Cid10LookupService cid10Lookup;
    private final DiagnosticoMapper mapper;

    public DiagnosticoService(DiagnosticoRepository repository,
                              AtendimentoLookupService atendimentoLookup,
                              Cid10LookupService cid10Lookup,
                              DiagnosticoMapper mapper) {
        this.repository = repository;
        this.atendimentoLookup = atendimentoLookup;
        this.cid10Lookup = cid10Lookup;
        this.mapper = mapper;
    }

    @Transactional
    public DiagnosticoResponse adicionar(Long atendimentoId, DiagnosticoRequest request,
                                         Long medicoId, String medicoNome) {
        AtendimentoDto atendimento = atendimentoLookup.buscar(atendimentoId);
        if (atendimento.status().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível adicionar diagnóstico.");
        }
        Cid10Dto cid = cid10Lookup.buscar(request.cid10Id());

        Diagnostico diagnostico = Diagnostico.builder()
                .atendimentoId(atendimentoId)
                .cid10Id(cid.id())
                .cid10Codigo(cid.codigo())
                .cid10Descricao(cid.descricao())
                .tipo(request.tipo())
                .medicoId(medicoId)
                .medicoNome(medicoNome)
                .observacao(request.observacao())
                .dataHora(LocalDateTime.now())
                .build();
        return mapper.toResponse(repository.save(diagnostico));
    }

    @Transactional(readOnly = true)
    public List<DiagnosticoResponse> listar(Long atendimentoId) {
        return repository.findByAtendimentoIdOrderByDataHoraDesc(atendimentoId)
                .stream().map(mapper::toResponse).toList();
    }
}
