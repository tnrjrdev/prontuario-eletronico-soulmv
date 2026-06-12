package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.AnamneseRequest;
import com.soulmv.hospitalar.dto.response.AnamneseResponse;
import com.soulmv.hospitalar.entity.Anamnese;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.ClinicoMapper;
import com.soulmv.hospitalar.repository.AnamneseRepository;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.service.support.UsuarioLookup;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AnamneseService {

    private final AnamneseRepository repository;
    private final AtendimentoRepository atendimentoRepository;
    private final ClinicoMapper mapper;
    private final UsuarioLookup usuarioLookup;

    public AnamneseService(AnamneseRepository repository,
                           AtendimentoRepository atendimentoRepository,
                           ClinicoMapper mapper,
                           UsuarioLookup usuarioLookup) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.mapper = mapper;
        this.usuarioLookup = usuarioLookup;
    }

    @Transactional
    public AnamneseResponse registrar(Long atendimentoId, AnamneseRequest request, String login) {
        Atendimento atendimento = obterAtendimento(atendimentoId);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível registrar anamnese.");
        }
        if (repository.existsByAtendimentoId(atendimentoId)) {
            throw new BusinessException("Este atendimento já possui anamnese.", HttpStatus.CONFLICT);
        }

        Anamnese anamnese = Anamnese.builder()
                .atendimento(atendimento)
                .medico(usuarioLookup.porLogin(login))
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

    private Atendimento obterAtendimento(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", id));
    }
}
