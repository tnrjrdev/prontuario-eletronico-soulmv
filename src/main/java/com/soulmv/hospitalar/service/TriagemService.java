package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.TriagemRequest;
import com.soulmv.hospitalar.dto.response.TriagemResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.Triagem;
import com.soulmv.hospitalar.enums.StatusAtendimento;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.EnfermagemMapper;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.TriagemRepository;
import com.soulmv.hospitalar.service.support.UsuarioLookup;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TriagemService {

    private final TriagemRepository triagemRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final EnfermagemMapper mapper;
    private final UsuarioLookup usuarioLookup;

    public TriagemService(TriagemRepository triagemRepository,
                          AtendimentoRepository atendimentoRepository,
                          EnfermagemMapper mapper,
                          UsuarioLookup usuarioLookup) {
        this.triagemRepository = triagemRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.mapper = mapper;
        this.usuarioLookup = usuarioLookup;
    }

    @Transactional
    public TriagemResponse registrar(Long atendimentoId, TriagemRequest request, String login) {
        Atendimento atendimento = obterAtendimento(atendimentoId);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível triar.");
        }
        if (triagemRepository.existsByAtendimentoId(atendimentoId)) {
            throw new BusinessException("Este atendimento já possui triagem.", HttpStatus.CONFLICT);
        }

        Triagem triagem = Triagem.builder()
                .atendimento(atendimento)
                .enfermeiro(usuarioLookup.porLogin(login))
                .classificacaoRisco(request.classificacaoRisco())
                .observacao(request.observacao())
                .dataHora(LocalDateTime.now())
                .build();
        triagem = triagemRepository.save(triagem);

        if (atendimento.getStatus() == StatusAtendimento.AGUARDANDO_TRIAGEM
                || atendimento.getStatus() == StatusAtendimento.EM_TRIAGEM) {
            atendimento.setStatus(StatusAtendimento.AGUARDANDO_ATENDIMENTO);
            atendimentoRepository.save(atendimento);
        }

        return mapper.toResponse(triagem);
    }

    @Transactional(readOnly = true)
    public TriagemResponse buscar(Long atendimentoId) {
        return triagemRepository.findByAtendimentoId(atendimentoId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Triagem do atendimento", atendimentoId));
    }

    private Atendimento obterAtendimento(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", id));
    }
}
