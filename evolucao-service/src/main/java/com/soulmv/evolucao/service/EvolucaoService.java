package com.soulmv.evolucao.service;

import com.soulmv.evolucao.client.AtendimentoDto;
import com.soulmv.evolucao.dto.request.EvolucaoRequest;
import com.soulmv.evolucao.dto.response.EvolucaoResponse;
import com.soulmv.evolucao.entity.EvolucaoClinica;
import com.soulmv.evolucao.enums.TipoEvolucao;
import com.soulmv.evolucao.exception.BusinessException;
import com.soulmv.evolucao.mapper.EvolucaoMapper;
import com.soulmv.evolucao.repository.EvolucaoClinicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvolucaoService {

    private final EvolucaoClinicaRepository repository;
    private final AtendimentoLookupService atendimentoLookup;
    private final EvolucaoMapper mapper;

    public EvolucaoService(EvolucaoClinicaRepository repository,
                           AtendimentoLookupService atendimentoLookup,
                           EvolucaoMapper mapper) {
        this.repository = repository;
        this.atendimentoLookup = atendimentoLookup;
        this.mapper = mapper;
    }

    @Transactional
    public EvolucaoResponse registrar(Long atendimentoId, EvolucaoRequest request,
                                      Long autorId, String autorNome, TipoEvolucao tipo) {
        AtendimentoDto atendimento = atendimentoLookup.buscar(atendimentoId);
        if (atendimento.status().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível registrar evolução.");
        }

        EvolucaoClinica evolucao = EvolucaoClinica.builder()
                .atendimentoId(atendimentoId)
                .tipo(tipo)
                .autorId(autorId)
                .autorNome(autorNome)
                .texto(request.texto())
                .dataHora(LocalDateTime.now())
                .build();
        return mapper.toResponse(repository.save(evolucao));
    }

    @Transactional(readOnly = true)
    public List<EvolucaoResponse> listar(Long atendimentoId) {
        return repository.findByAtendimentoIdOrderByDataHoraDesc(atendimentoId)
                .stream().map(mapper::toResponse).toList();
    }
}
