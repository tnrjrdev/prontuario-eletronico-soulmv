package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.EvolucaoRequest;
import com.soulmv.hospitalar.dto.response.EvolucaoResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.EvolucaoClinica;
import com.soulmv.hospitalar.enums.TipoEvolucao;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.EnfermagemMapper;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.EvolucaoClinicaRepository;
import com.soulmv.hospitalar.service.support.UsuarioLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvolucaoService {

    private final EvolucaoClinicaRepository repository;
    private final AtendimentoRepository atendimentoRepository;
    private final EnfermagemMapper mapper;
    private final UsuarioLookup usuarioLookup;

    public EvolucaoService(EvolucaoClinicaRepository repository,
                           AtendimentoRepository atendimentoRepository,
                           EnfermagemMapper mapper,
                           UsuarioLookup usuarioLookup) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.mapper = mapper;
        this.usuarioLookup = usuarioLookup;
    }

    /**
     * Registra uma evolução. O {@code tipo} é definido pelo perfil do autor
     * (MÉDICO → MEDICA, ENFERMEIRO → ENFERMAGEM), garantindo que ninguém
     * registre evolução de outra natureza.
     */
    @Transactional
    public EvolucaoResponse registrar(Long atendimentoId, EvolucaoRequest request,
                                      String login, TipoEvolucao tipo) {
        Atendimento atendimento = obterAtendimento(atendimentoId);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível registrar evolução.");
        }

        EvolucaoClinica evolucao = EvolucaoClinica.builder()
                .atendimento(atendimento)
                .tipo(tipo)
                .autor(usuarioLookup.porLogin(login))
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

    private Atendimento obterAtendimento(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", id));
    }
}
