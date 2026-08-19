package com.soulmv.sinaisvitais.service;

import com.soulmv.sinaisvitais.client.AtendimentoDto;
import com.soulmv.sinaisvitais.dto.request.SinaisVitaisRequest;
import com.soulmv.sinaisvitais.dto.response.SinaisVitaisResponse;
import com.soulmv.sinaisvitais.entity.SinaisVitais;
import com.soulmv.sinaisvitais.exception.BusinessException;
import com.soulmv.sinaisvitais.mapper.SinaisVitaisMapper;
import com.soulmv.sinaisvitais.repository.SinaisVitaisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SinaisVitaisService {

    private final SinaisVitaisRepository repository;
    private final AtendimentoLookupService atendimentoLookup;
    private final SinaisVitaisMapper mapper;

    public SinaisVitaisService(SinaisVitaisRepository repository,
                               AtendimentoLookupService atendimentoLookup,
                               SinaisVitaisMapper mapper) {
        this.repository = repository;
        this.atendimentoLookup = atendimentoLookup;
        this.mapper = mapper;
    }

    @Transactional
    public SinaisVitaisResponse registrar(Long atendimentoId, SinaisVitaisRequest request,
                                          Long registradoPorId, String registradoPorNome) {
        AtendimentoDto atendimento = atendimentoLookup.buscar(atendimentoId);
        if (atendimento.status().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível registrar sinais vitais.");
        }

        SinaisVitais sinais = SinaisVitais.builder()
                .atendimentoId(atendimentoId)
                .registradoPorId(registradoPorId)
                .registradoPorNome(registradoPorNome)
                .pressaoSistolica(request.pressaoSistolica())
                .pressaoDiastolica(request.pressaoDiastolica())
                .frequenciaCardiaca(request.frequenciaCardiaca())
                .frequenciaRespiratoria(request.frequenciaRespiratoria())
                .temperatura(request.temperatura())
                .saturacaoO2(request.saturacaoO2())
                .glicemia(request.glicemia())
                .escalaDor(request.escalaDor())
                .dataHora(LocalDateTime.now())
                .build();
        return mapper.toResponse(repository.save(sinais));
    }

    @Transactional(readOnly = true)
    public List<SinaisVitaisResponse> listar(Long atendimentoId) {
        return repository.findByAtendimentoIdOrderByDataHoraDesc(atendimentoId)
                .stream().map(mapper::toResponse).toList();
    }
}
