package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.AgendamentoRequest;
import com.soulmv.hospitalar.dto.request.AgendamentoStatusRequest;
import com.soulmv.hospitalar.dto.response.AgendamentoResponse;
import com.soulmv.hospitalar.entity.Agendamento;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.Convenio;
import com.soulmv.hospitalar.entity.Paciente;
import com.soulmv.hospitalar.entity.Setor;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.enums.Role;
import com.soulmv.hospitalar.enums.StatusAgendamento;
import com.soulmv.hospitalar.enums.StatusAtendimento;
import com.soulmv.hospitalar.enums.TipoAgendamento;
import com.soulmv.hospitalar.enums.TipoAtendimento;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.AgendamentoMapper;
import com.soulmv.hospitalar.repository.AgendamentoRepository;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.ConvenioRepository;
import com.soulmv.hospitalar.repository.PacienteRepository;
import com.soulmv.hospitalar.repository.SetorRepository;
import com.soulmv.hospitalar.repository.UsuarioRepository;
import com.soulmv.hospitalar.repository.spec.AgendamentoSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Regras de negócio da agenda: marcação, reagendamento, transição de status,
 * checagem de conflito de horário do profissional e check-in (conversão do
 * agendamento em um atendimento/encontro).
 */
@Service
public class AgendamentoService {

    private static final Set<StatusAgendamento> STATUS_ATIVOS =
            Set.of(StatusAgendamento.AGENDADO, StatusAgendamento.CONFIRMADO);

    private final AgendamentoRepository repository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SetorRepository setorRepository;
    private final ConvenioRepository convenioRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final AgendamentoMapper mapper;

    public AgendamentoService(AgendamentoRepository repository,
                              PacienteRepository pacienteRepository,
                              UsuarioRepository usuarioRepository,
                              SetorRepository setorRepository,
                              ConvenioRepository convenioRepository,
                              AtendimentoRepository atendimentoRepository,
                              AgendamentoMapper mapper) {
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.setorRepository = setorRepository;
        this.convenioRepository = convenioRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> listar(Long profissionalId, Long pacienteId, Long setorId,
                                            StatusAgendamento status, TipoAgendamento tipo,
                                            LocalDateTime de, LocalDateTime ate, Pageable pageable) {
        Specification<Agendamento> spec = Specification
                .where(AgendamentoSpecs.profissionalId(profissionalId))
                .and(AgendamentoSpecs.pacienteId(pacienteId))
                .and(AgendamentoSpecs.setorId(setorId))
                .and(AgendamentoSpecs.status(status))
                .and(AgendamentoSpecs.tipo(tipo))
                .and(AgendamentoSpecs.dataHoraApartirDe(de))
                .and(AgendamentoSpecs.dataHoraAte(ate));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest request) {
        Paciente paciente = obterPaciente(request.pacienteId());
        Usuario profissional = obterProfissional(request.profissionalId());
        Setor setor = obterSetor(request.setorId());
        Convenio convenio = obterConvenioOpcional(request.convenioId());
        int duracao = request.duracaoMinutos() != null ? request.duracaoMinutos() : 30;

        validarConflito(profissional.getId(), request.dataHora(), duracao, null);

        Agendamento agendamento = Agendamento.builder()
                .paciente(paciente)
                .profissional(profissional)
                .setor(setor)
                .convenio(convenio)
                .tipo(request.tipo())
                .status(StatusAgendamento.AGENDADO)
                .dataHora(request.dataHora())
                .duracaoMinutos(duracao)
                .observacoes(request.observacoes())
                .build();
        return mapper.toResponse(repository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse atualizar(Long id, AgendamentoRequest request) {
        Agendamento agendamento = obter(id);
        if (agendamento.getStatus().isFinal()) {
            throw new BusinessException("Agendamento encerrado; não é possível reagendar.");
        }

        Paciente paciente = obterPaciente(request.pacienteId());
        Usuario profissional = obterProfissional(request.profissionalId());
        Setor setor = obterSetor(request.setorId());
        Convenio convenio = obterConvenioOpcional(request.convenioId());
        int duracao = request.duracaoMinutos() != null ? request.duracaoMinutos() : 30;

        validarConflito(profissional.getId(), request.dataHora(), duracao, agendamento.getId());

        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setSetor(setor);
        agendamento.setConvenio(convenio);
        agendamento.setTipo(request.tipo());
        agendamento.setDataHora(request.dataHora());
        agendamento.setDuracaoMinutos(duracao);
        agendamento.setObservacoes(request.observacoes());
        return mapper.toResponse(repository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse atualizarStatus(Long id, AgendamentoStatusRequest request) {
        Agendamento agendamento = obter(id);
        StatusAgendamento novo = request.status();

        if (novo == StatusAgendamento.REALIZADO) {
            throw new BusinessException("Use o check-in para marcar o agendamento como realizado.");
        }
        if (agendamento.getStatus().isFinal()) {
            throw new BusinessException("Agendamento encerrado; não admite nova transição de status.");
        }

        agendamento.setStatus(novo);
        return mapper.toResponse(repository.save(agendamento));
    }

    /**
     * Check-in: converte o agendamento em um atendimento (encontro) na fila de
     * atendimento e marca o agendamento como REALIZADO.
     */
    @Transactional
    public AgendamentoResponse checkin(Long id) {
        Agendamento agendamento = obter(id);
        if (!agendamento.getStatus().permiteCheckin()) {
            throw new BusinessException("Check-in indisponível para o status atual: " + agendamento.getStatus() + ".");
        }
        if (agendamento.getAtendimento() != null) {
            throw new BusinessException("Check-in já realizado para este agendamento.", HttpStatus.CONFLICT);
        }

        Atendimento atendimento = Atendimento.builder()
                .paciente(agendamento.getPaciente())
                .tipo(TipoAtendimento.AMBULATORIAL)
                .setor(agendamento.getSetor())
                .status(StatusAtendimento.AGUARDANDO_ATENDIMENTO)
                .profissionalResponsavel(agendamento.getProfissional())
                .queixaPrincipal(agendamento.getObservacoes())
                .dataEntrada(LocalDateTime.now())
                .build();
        atendimento = atendimentoRepository.save(atendimento);

        agendamento.setAtendimento(atendimento);
        agendamento.setStatus(StatusAgendamento.REALIZADO);
        return mapper.toResponse(repository.save(agendamento));
    }

    /**
     * Conflito quando o profissional já tem outro compromisso ativo cujo intervalo
     * [início, fim) se sobrepõe ao novo. {@code ignorarId} exclui o próprio registro
     * em reagendamentos.
     */
    private void validarConflito(Long profissionalId, LocalDateTime inicio, int duracaoMinutos, Long ignorarId) {
        LocalDateTime fim = inicio.plusMinutes(duracaoMinutos);
        LocalDate dia = inicio.toLocalDate();
        LocalDateTime inicioDia = dia.atStartOfDay();
        LocalDateTime fimDia = dia.atTime(LocalTime.MAX);

        List<Agendamento> doDia = repository.findByProfissionalIdAndStatusInAndDataHoraBetween(
                profissionalId, STATUS_ATIVOS, inicioDia, fimDia);

        boolean conflito = doDia.stream()
                .filter(a -> !a.getId().equals(ignorarId))
                .anyMatch(a -> inicio.isBefore(a.getFimPrevisto()) && a.getDataHora().isBefore(fim));
        if (conflito) {
            throw new BusinessException("O profissional já possui um agendamento neste horário.", HttpStatus.CONFLICT);
        }
    }

    private Paciente obterPaciente(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
    }

    private Usuario obterProfissional(Long id) {
        Usuario profissional = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", id));
        if (!profissional.isAtivo()) {
            throw new BusinessException("Profissional inativo.");
        }
        boolean clinico = profissional.getRoles().contains(Role.MEDICO)
                || profissional.getRoles().contains(Role.ENFERMEIRO);
        if (!clinico) {
            throw new BusinessException("O usuário selecionado não é um profissional de saúde (médico/enfermeiro).");
        }
        return profissional;
    }

    private Setor obterSetor(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setor", id));
    }

    private Convenio obterConvenioOpcional(Long id) {
        if (id == null) {
            return null;
        }
        return convenioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convênio", id));
    }

    private Agendamento obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
    }
}
