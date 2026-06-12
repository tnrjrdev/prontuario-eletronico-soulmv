package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.AlocarLeitoRequest;
import com.soulmv.hospitalar.dto.request.AtendimentoRequest;
import com.soulmv.hospitalar.dto.request.AtendimentoStatusRequest;
import com.soulmv.hospitalar.dto.response.AtendimentoResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.Leito;
import com.soulmv.hospitalar.entity.Paciente;
import com.soulmv.hospitalar.entity.Setor;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.enums.StatusAtendimento;
import com.soulmv.hospitalar.enums.StatusLeito;
import com.soulmv.hospitalar.enums.TipoAtendimento;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.AtendimentoMapper;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.LeitoRepository;
import com.soulmv.hospitalar.repository.PacienteRepository;
import com.soulmv.hospitalar.repository.SetorRepository;
import com.soulmv.hospitalar.repository.UsuarioRepository;
import com.soulmv.hospitalar.repository.spec.AtendimentoSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Regras de negócio do atendimento: abertura, fila, transição de status,
 * alocação de leito (internação) e alta.
 */
@Service
public class AtendimentoService {

    private final AtendimentoRepository repository;
    private final PacienteRepository pacienteRepository;
    private final SetorRepository setorRepository;
    private final LeitoRepository leitoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AtendimentoMapper mapper;

    public AtendimentoService(AtendimentoRepository repository,
                              PacienteRepository pacienteRepository,
                              SetorRepository setorRepository,
                              LeitoRepository leitoRepository,
                              UsuarioRepository usuarioRepository,
                              AtendimentoMapper mapper) {
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;
        this.setorRepository = setorRepository;
        this.leitoRepository = leitoRepository;
        this.usuarioRepository = usuarioRepository;
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
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", request.pacienteId()));
        Setor setor = setorRepository.findById(request.setorId())
                .orElseThrow(() -> new ResourceNotFoundException("Setor", request.setorId()));

        Atendimento atendimento = Atendimento.builder()
                .paciente(paciente)
                .tipo(request.tipo())
                .setor(setor)
                .status(StatusAtendimento.AGUARDANDO_TRIAGEM)
                .queixaPrincipal(request.queixaPrincipal())
                .dataEntrada(LocalDateTime.now())
                .build();
        return mapper.toResponse(repository.save(atendimento));
    }

    @Transactional
    public AtendimentoResponse atualizarStatus(Long id, AtendimentoStatusRequest request, String loginAtual) {
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
        if (novo == StatusAtendimento.EM_ATENDIMENTO && atendimento.getProfissionalResponsavel() == null) {
            atendimento.setProfissionalResponsavel(usuarioLogado(loginAtual));
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

        Leito leito = leitoRepository.findById(request.leitoId())
                .orElseThrow(() -> new ResourceNotFoundException("Leito", request.leitoId()));
        if (!leito.isAtivo()) {
            throw new BusinessException("Leito inativo.");
        }
        if (leito.getStatus() != StatusLeito.LIVRE) {
            throw new BusinessException("Leito indisponível (status atual: " + leito.getStatus() + ").",
                    HttpStatus.CONFLICT);
        }

        // libera leito anterior, se houver
        liberarLeito(atendimento);

        leito.setStatus(StatusLeito.OCUPADO);
        leitoRepository.save(leito);

        atendimento.setLeito(leito);
        atendimento.setStatus(StatusAtendimento.INTERNADO);
        return mapper.toResponse(repository.save(atendimento));
    }

    @Transactional
    public AtendimentoResponse darAlta(Long id, String loginAtual) {
        Atendimento atendimento = obter(id);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento já encerrado.");
        }
        if (atendimento.getProfissionalResponsavel() == null) {
            atendimento.setProfissionalResponsavel(usuarioLogado(loginAtual));
        }
        liberarLeito(atendimento);
        atendimento.setStatus(StatusAtendimento.ALTA);
        atendimento.setDataAlta(LocalDateTime.now());
        return mapper.toResponse(repository.save(atendimento));
    }

    private void liberarLeito(Atendimento atendimento) {
        Leito leito = atendimento.getLeito();
        if (leito != null) {
            leito.setStatus(StatusLeito.HIGIENIZACAO);
            leitoRepository.save(leito);
            atendimento.setLeito(null);
        }
    }

    private Usuario usuarioLogado(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", login));
    }

    private Atendimento obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", id));
    }
}
