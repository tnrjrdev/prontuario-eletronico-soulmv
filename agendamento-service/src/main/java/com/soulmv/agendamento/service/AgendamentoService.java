package com.soulmv.agendamento.service;

import com.soulmv.agendamento.client.ConvenioDto;
import com.soulmv.agendamento.client.PacienteDto;
import com.soulmv.agendamento.client.ProfissionalDto;
import com.soulmv.agendamento.client.SetorDto;
import com.soulmv.agendamento.dto.request.AgendamentoRequest;
import com.soulmv.agendamento.dto.request.AgendamentoStatusRequest;
import com.soulmv.agendamento.dto.response.AgendamentoResponse;
import com.soulmv.agendamento.entity.Agendamento;
import com.soulmv.agendamento.entity.Atendimento;
import com.soulmv.agendamento.entity.Paciente;
import com.soulmv.agendamento.entity.Setor;
import com.soulmv.agendamento.entity.Usuario;
import com.soulmv.agendamento.enums.StatusAgendamento;
import com.soulmv.agendamento.enums.StatusAtendimento;
import com.soulmv.agendamento.enums.TipoAgendamento;
import com.soulmv.agendamento.enums.TipoAtendimento;
import com.soulmv.agendamento.exception.BusinessException;
import com.soulmv.agendamento.exception.ResourceNotFoundException;
import com.soulmv.agendamento.mapper.AgendamentoMapper;
import com.soulmv.agendamento.repository.AgendamentoRepository;
import com.soulmv.agendamento.repository.AtendimentoRepository;
import com.soulmv.agendamento.repository.spec.AgendamentoSpecs;
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
 *
 * <p>Paciente, profissional, setor e convênio são validados via Feign nos serviços
 * donos (paciente-service/iam-service/catalogo-service) — não são mais lidos do banco
 * compartilhado. O Atendimento gerado no check-in continua sendo escrito direto no
 * banco compartilhado: esse domínio ainda não foi extraído do monólito.</p>
 */
@Service
public class AgendamentoService {

    private static final Set<StatusAgendamento> STATUS_ATIVOS =
            Set.of(StatusAgendamento.AGENDADO, StatusAgendamento.CONFIRMADO);

    private final AgendamentoRepository repository;
    private final AtendimentoRepository atendimentoRepository;
    private final PacienteLookupService pacienteLookup;
    private final ProfissionalValidationService profissionalValidation;
    private final SetorLookupService setorLookup;
    private final ConvenioLookupService convenioLookup;
    private final AgendamentoMapper mapper;

    public AgendamentoService(AgendamentoRepository repository,
                              AtendimentoRepository atendimentoRepository,
                              PacienteLookupService pacienteLookup,
                              ProfissionalValidationService profissionalValidation,
                              SetorLookupService setorLookup,
                              ConvenioLookupService convenioLookup,
                              AgendamentoMapper mapper) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.pacienteLookup = pacienteLookup;
        this.profissionalValidation = profissionalValidation;
        this.setorLookup = setorLookup;
        this.convenioLookup = convenioLookup;
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
        PacienteDto paciente = pacienteLookup.buscar(request.pacienteId());
        ProfissionalDto profissional = profissionalValidation.validar(request.profissionalId());
        SetorDto setor = setorLookup.buscar(request.setorId());
        ConvenioDto convenio = obterConvenioOpcional(request.convenioId());
        int duracao = request.duracaoMinutos() != null ? request.duracaoMinutos() : 30;

        validarConflito(profissional.id(), request.dataHora(), duracao, null);

        Agendamento agendamento = Agendamento.builder()
                .pacienteId(paciente.id())
                .pacienteNome(paciente.nome())
                .profissionalId(profissional.id())
                .profissionalNome(profissional.nome())
                .setorId(setor.id())
                .setorNome(setor.nome())
                .convenioId(convenio != null ? convenio.id() : null)
                .convenioNome(convenio != null ? convenio.nome() : null)
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

        PacienteDto paciente = pacienteLookup.buscar(request.pacienteId());
        ProfissionalDto profissional = profissionalValidation.validar(request.profissionalId());
        SetorDto setor = setorLookup.buscar(request.setorId());
        ConvenioDto convenio = obterConvenioOpcional(request.convenioId());
        int duracao = request.duracaoMinutos() != null ? request.duracaoMinutos() : 30;

        validarConflito(profissional.id(), request.dataHora(), duracao, agendamento.getId());

        agendamento.setPacienteId(paciente.id());
        agendamento.setPacienteNome(paciente.nome());
        agendamento.setProfissionalId(profissional.id());
        agendamento.setProfissionalNome(profissional.nome());
        agendamento.setSetorId(setor.id());
        agendamento.setSetorNome(setor.nome());
        agendamento.setConvenioId(convenio != null ? convenio.id() : null);
        agendamento.setConvenioNome(convenio != null ? convenio.nome() : null);
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
     * atendimento e marca o agendamento como REALIZADO. O Atendimento ainda vive no
     * banco compartilhado (domínio não extraído do monólito); as referências de
     * paciente/setor/profissional usadas aqui são objetos "somente FK" — só carregam o
     * id, o suficiente para o Hibernate gravar a coluna de chave estrangeira, sem
     * precisar buscar essas entidades de volta no banco.
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
                .paciente(referenciaPaciente(agendamento.getPacienteId()))
                .tipo(TipoAtendimento.AMBULATORIAL)
                .setor(referenciaSetor(agendamento.getSetorId()))
                .status(StatusAtendimento.AGUARDANDO_ATENDIMENTO)
                .profissionalResponsavel(referenciaProfissional(agendamento.getProfissionalId()))
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

    private ConvenioDto obterConvenioOpcional(Long id) {
        if (id == null) {
            return null;
        }
        return convenioLookup.buscar(id);
    }

    private Paciente referenciaPaciente(Long id) {
        Paciente ref = new Paciente();
        ref.setId(id);
        return ref;
    }

    private Setor referenciaSetor(Long id) {
        Setor ref = new Setor();
        ref.setId(id);
        return ref;
    }

    private Usuario referenciaProfissional(Long id) {
        Usuario ref = new Usuario();
        ref.setId(id);
        return ref;
    }

    private Agendamento obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
    }
}
