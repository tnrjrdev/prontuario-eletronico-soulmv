package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.DiagnosticoRequest;
import com.soulmv.hospitalar.dto.response.DiagnosticoResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.Cid10;
import com.soulmv.hospitalar.entity.Diagnostico;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.ClinicoMapper;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.Cid10Repository;
import com.soulmv.hospitalar.repository.DiagnosticoRepository;
import com.soulmv.hospitalar.service.support.UsuarioLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository repository;
    private final AtendimentoRepository atendimentoRepository;
    private final Cid10Repository cid10Repository;
    private final ClinicoMapper mapper;
    private final UsuarioLookup usuarioLookup;

    public DiagnosticoService(DiagnosticoRepository repository,
                              AtendimentoRepository atendimentoRepository,
                              Cid10Repository cid10Repository,
                              ClinicoMapper mapper,
                              UsuarioLookup usuarioLookup) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.cid10Repository = cid10Repository;
        this.mapper = mapper;
        this.usuarioLookup = usuarioLookup;
    }

    @Transactional
    public DiagnosticoResponse adicionar(Long atendimentoId, DiagnosticoRequest request, String login) {
        Atendimento atendimento = obterAtendimento(atendimentoId);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível adicionar diagnóstico.");
        }
        Cid10 cid = cid10Repository.findById(request.cid10Id())
                .orElseThrow(() -> new ResourceNotFoundException("CID-10", request.cid10Id()));

        Diagnostico diagnostico = Diagnostico.builder()
                .atendimento(atendimento)
                .cid10(cid)
                .tipo(request.tipo())
                .medico(usuarioLookup.porLogin(login))
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

    private Atendimento obterAtendimento(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", id));
    }
}
