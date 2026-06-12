package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.SinaisVitaisRequest;
import com.soulmv.hospitalar.dto.response.SinaisVitaisResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.SinaisVitais;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.EnfermagemMapper;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.SinaisVitaisRepository;
import com.soulmv.hospitalar.service.support.UsuarioLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SinaisVitaisService {

    private final SinaisVitaisRepository repository;
    private final AtendimentoRepository atendimentoRepository;
    private final EnfermagemMapper mapper;
    private final UsuarioLookup usuarioLookup;

    public SinaisVitaisService(SinaisVitaisRepository repository,
                               AtendimentoRepository atendimentoRepository,
                               EnfermagemMapper mapper,
                               UsuarioLookup usuarioLookup) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.mapper = mapper;
        this.usuarioLookup = usuarioLookup;
    }

    @Transactional
    public SinaisVitaisResponse registrar(Long atendimentoId, SinaisVitaisRequest request, String login) {
        Atendimento atendimento = obterAtendimento(atendimentoId);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível registrar sinais vitais.");
        }

        SinaisVitais sinais = SinaisVitais.builder()
                .atendimento(atendimento)
                .registradoPor(usuarioLookup.porLogin(login))
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

    private Atendimento obterAtendimento(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", id));
    }
}
