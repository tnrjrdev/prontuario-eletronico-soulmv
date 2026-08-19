package com.soulmv.atendimento.service;

import com.soulmv.atendimento.client.LeitoDto;
import com.soulmv.atendimento.client.PacienteDto;
import com.soulmv.atendimento.client.SetorDto;
import com.soulmv.atendimento.dto.request.AlocarLeitoRequest;
import com.soulmv.atendimento.dto.request.AtendimentoRequest;
import com.soulmv.atendimento.dto.request.AtendimentoStatusRequest;
import com.soulmv.atendimento.dto.response.AtendimentoResponse;
import com.soulmv.atendimento.entity.Atendimento;
import com.soulmv.atendimento.enums.StatusAtendimento;
import com.soulmv.atendimento.enums.StatusLeito;
import com.soulmv.atendimento.enums.TipoAtendimento;
import com.soulmv.atendimento.exception.BusinessException;
import com.soulmv.atendimento.exception.ResourceNotFoundException;
import com.soulmv.atendimento.mapper.AtendimentoMapper;
import com.soulmv.atendimento.repository.AtendimentoRepository;
import com.soulmv.atendimento.repository.spec.AtendimentoSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Regras de negócio do atendimento: abertura, fila, transição de status,
 * alocação de leito (internação) e alta.
 *
 * <p>Paciente e setor são validados via Feign nos serviços donos (paciente-service,
 * catalogo-service). Leito é uma transição de estado remota atômica no
 * catalogo-service (não um "buscar, checar, gravar" local) — ver
 * {@link LeitoLookupService}.</p>
 */
@Service
public class AtendimentoService {

    private final AtendimentoRepository repository;
    private final PacienteLookupService pacienteLookup;
    private final SetorLookupService setorLookup;
    private final LeitoLookupService leitoLookup;
    private final AtendimentoMapper mapper;

    public AtendimentoService(AtendimentoRepository repository,
                              PacienteLookupService pacienteLookup,
                              SetorLookupService setorLookup,
                              LeitoLookupService leitoLookup,
                              AtendimentoMapper mapper) {
        this.repository = repository;
        this.pacienteLookup = pacienteLookup;
        this.setorLookup = setorLookup;
        this.leitoLookup = leitoLookup;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<AtendimentoResponse> listar(StatusAtendimento status, TipoAtendimento tipo,
                                            Long setorId, Long pacienteId, Pageable pageable) {
        Specification<Atendimento> spec = Specification
                .where(AtendimentoSpecs.status(status))
                .and(AtendimentoSpecs.tipo(tipo))
                .and(AtendimentoSpecs.setorId(setorId))
                .and(AtendimentoSpecs.pacienteId(pacienteId));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AtendimentoResponse buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public AtendimentoResponse abrir(AtendimentoRequest request) {
        PacienteDto paciente = pacienteLookup.buscar(request.pacienteId());
        SetorDto setor = setorLookup.buscar(request.setorId());

        Atendimento atendimento = Atendimento.builder()
                .pacienteId(paciente.id())
                .pacienteNome(paciente.nome())
                .tipo(request.tipo())
                .setorId(setor.id())
                .setorNome(setor.nome())
                .status(StatusAtendimento.AGUARDANDO_TRIAGEM)
                .queixaPrincipal(request.queixaPrincipal())
                .dataEntrada(LocalDateTime.now())
                .build();
        return mapper.toResponse(repository.save(atendimento));
    }

    @Transactional
    public AtendimentoResponse atualizarStatus(Long id, AtendimentoStatusRequest request,
                                               Long usuarioId, String usuarioNome) {
        Atendimento atendimento = obter(id);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento já encerrado; não é possível alterar o status.");
        }

        StatusAtendimento novo = request.status();
        if (novo == StatusAtendimento.ALTA) {
            throw new BusinessException("Use o endpoint de alta para encerrar o atendimento.");
        }
        if (novo == StatusAtendimento.CANCELADO) {
            liberarLeito(atendimento);
        }
        if (novo == StatusAtendimento.EM_ATENDIMENTO && atendimento.getProfissionalResponsavelId() == null) {
            atendimento.setProfissionalResponsavelId(usuarioId);
            atendimento.setProfissionalResponsavelNome(usuarioNome);
        }

        atendimento.setStatus(novo);
        return mapper.toResponse(repository.save(atendimento));
    }

    @Transactional
    public AtendimentoResponse alocarLeito(Long id, AlocarLeitoRequest request) {
        Atendimento atendimento = obter(id);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível alocar leito.");
        }

        // Valida + ocupa o novo leito primeiro (atômico no catalogo-service); só libera
        // o leito anterior depois de confirmado, pra não deixar o paciente sem leito
        // nenhum caso a nova alocação falhe.
        LeitoDto novoLeito = leitoLookup.atualizarStatus(request.leitoId(), StatusLeito.OCUPADO);

        liberarLeito(atendimento);

        atendimento.setLeitoId(novoLeito.id());
        atendimento.setLeitoIdentificador(novoLeito.identificador());
        atendimento.setStatus(StatusAtendimento.INTERNADO);
        return mapper.toResponse(repository.save(atendimento));
    }

    @Transactional
    public AtendimentoResponse darAlta(Long id, Long usuarioId, String usuarioNome) {
        Atendimento atendimento = obter(id);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento já encerrado.");
        }
        if (atendimento.getProfissionalResponsavelId() == null) {
            atendimento.setProfissionalResponsavelId(usuarioId);
            atendimento.setProfissionalResponsavelNome(usuarioNome);
        }
        liberarLeito(atendimento);
        atendimento.setStatus(StatusAtendimento.ALTA);
        atendimento.setDataAlta(LocalDateTime.now());
        return mapper.toResponse(repository.save(atendimento));
    }

    private void liberarLeito(Atendimento atendimento) {
        if (atendimento.getLeitoId() != null) {
            leitoLookup.atualizarStatus(atendimento.getLeitoId(), StatusLeito.HIGIENIZACAO);
            atendimento.setLeitoId(null);
            atendimento.setLeitoIdentificador(null);
        }
    }

    private Atendimento obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", id));
    }
}
