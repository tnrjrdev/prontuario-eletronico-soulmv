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
import com.soulmv.agendamento.enums.StatusAgendamento;
import com.soulmv.agendamento.enums.StatusAtendimento;
import com.soulmv.agendamento.enums.TipoAgendamento;
import com.soulmv.agendamento.exception.BusinessException;
import com.soulmv.agendamento.exception.ResourceNotFoundException;
import com.soulmv.agendamento.mapper.AgendamentoMapper;
import com.soulmv.agendamento.repository.AgendamentoRepository;
import com.soulmv.agendamento.repository.AtendimentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock AgendamentoRepository repository;
    @Mock AtendimentoRepository atendimentoRepository;
    @Mock PacienteLookupService pacienteLookup;
    @Mock ProfissionalValidationService profissionalValidation;
    @Mock SetorLookupService setorLookup;
    @Mock ConvenioLookupService convenioLookup;
    @Mock AgendamentoMapper mapper;

    @InjectMocks
    AgendamentoService service;

    private static final LocalDateTime HORARIO_BASE =
            LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

    private void stubDependenciasBasicas(Long pacienteId, Long profissionalId, Long setorId) {
        when(pacienteLookup.buscar(pacienteId)).thenReturn(new PacienteDto(pacienteId, "Paciente " + pacienteId));
        when(profissionalValidation.validar(profissionalId))
                .thenReturn(new ProfissionalDto(profissionalId, "Dr. Fulano", Set.of("MEDICO")));
        when(setorLookup.buscar(setorId)).thenReturn(new SetorDto(setorId, "Ambulatório"));
    }

    private Agendamento agendamentoExistente(Long id, Long profissionalId, LocalDateTime dataHora,
                                              int duracao, StatusAgendamento status) {
        Agendamento agendamento = Agendamento.builder()
                .pacienteId(100L).pacienteNome("Paciente 100")
                .profissionalId(profissionalId).profissionalNome("Dr. Fulano")
                .setorId(200L).setorNome("Ambulatório")
                .tipo(TipoAgendamento.CONSULTA)
                .status(status)
                .dataHora(dataHora)
                .duracaoMinutos(duracao)
                .build();
        agendamento.setId(id);
        return agendamento;
    }

    private AgendamentoRequest request(Long pacienteId, Long profissionalId, Long setorId,
                                        LocalDateTime dataHora, Integer duracao) {
        return new AgendamentoRequest(pacienteId, profissionalId, setorId, null,
                TipoAgendamento.CONSULTA, dataHora, duracao, "Observação de teste");
    }

    // ---------- criar ----------

    @Test
    void criar_deveSalvarAgendamento_quandoSemConflito() {
        Long pacienteId = 1L, profissionalId = 2L, setorId = 3L;
        stubDependenciasBasicas(pacienteId, profissionalId, setorId);
        when(repository.findByProfissionalIdAndStatusInAndDataHoraBetween(
                anyLong(), anyCollection(), any(), any())).thenReturn(List.of());
        when(repository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));
        AgendamentoResponse resposta = new AgendamentoResponse(
                1L, pacienteId, "Paciente 1", profissionalId, "Dr. Fulano", setorId, "Ambulatório",
                null, null, TipoAgendamento.CONSULTA, StatusAgendamento.AGENDADO, HORARIO_BASE, 30,
                "Observação de teste", null, null, null);
        when(mapper.toResponse(any(Agendamento.class))).thenReturn(resposta);

        AgendamentoRequest request = request(pacienteId, profissionalId, setorId, HORARIO_BASE, null);
        AgendamentoResponse resultado = service.criar(request);

        assertThat(resultado).isEqualTo(resposta);

        ArgumentCaptor<Agendamento> captor = ArgumentCaptor.forClass(Agendamento.class);
        verify(repository).save(captor.capture());
        Agendamento salvo = captor.getValue();
        assertThat(salvo.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(salvo.getDuracaoMinutos()).isEqualTo(30);
        assertThat(salvo.getDataHora()).isEqualTo(HORARIO_BASE);
        assertThat(salvo.getPacienteNome()).isEqualTo("Paciente 1");
        assertThat(salvo.getProfissionalNome()).isEqualTo("Dr. Fulano");
        assertThat(salvo.getSetorNome()).isEqualTo("Ambulatório");
    }

    @Test
    void criar_deveLancarBusinessException_quandoConflitaComAgendamentoExistente() {
        Long pacienteId = 1L, profissionalId = 2L, setorId = 3L;
        stubDependenciasBasicas(pacienteId, profissionalId, setorId);

        // Agendamento existente das 10:00 às 10:30 (mesmo profissional, status ativo).
        Agendamento existente = agendamentoExistente(50L, profissionalId, HORARIO_BASE, 30,
                StatusAgendamento.AGENDADO);
        when(repository.findByProfissionalIdAndStatusInAndDataHoraBetween(
                anyLong(), anyCollection(), any(), any())).thenReturn(List.of(existente));

        // Nova tentativa às 10:15 (sobrepõe o existente).
        AgendamentoRequest request = request(pacienteId, profissionalId, setorId,
                HORARIO_BASE.plusMinutes(15), 30);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já possui um agendamento");

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void criar_naoDeveLancarConflito_quandoHorariosNaoSeSobrepoem() {
        Long pacienteId = 1L, profissionalId = 2L, setorId = 3L;
        stubDependenciasBasicas(pacienteId, profissionalId, setorId);

        // Agendamento existente das 10:00 às 10:30.
        Agendamento existente = agendamentoExistente(50L, profissionalId, HORARIO_BASE, 30,
                StatusAgendamento.AGENDADO);
        when(repository.findByProfissionalIdAndStatusInAndDataHoraBetween(
                anyLong(), anyCollection(), any(), any())).thenReturn(List.of(existente));
        when(repository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Agendamento.class))).thenReturn(null);

        // Nova tentativa às 10:30 (exatamente quando o existente termina — não sobrepõe).
        AgendamentoRequest request = request(pacienteId, profissionalId, setorId,
                HORARIO_BASE.plusMinutes(30), 30);

        service.criar(request);

        verify(repository).save(any(Agendamento.class));
    }

    @Test
    void criar_deveLancarBusinessException_quandoProfissionalInvalido() {
        Long pacienteId = 1L, profissionalId = 2L, setorId = 3L;
        when(pacienteLookup.buscar(pacienteId)).thenReturn(new PacienteDto(pacienteId, "Paciente 1"));
        when(profissionalValidation.validar(profissionalId)).thenThrow(
                new BusinessException("Profissional não encontrado, inativo, ou sem perfil clínico (médico/enfermeiro)."));

        AgendamentoRequest request = request(pacienteId, profissionalId, setorId, HORARIO_BASE, null);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Profissional");

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void criar_deveLancarResourceNotFoundException_quandoPacienteNaoExiste() {
        Long pacienteId = 999L, profissionalId = 2L, setorId = 3L;
        when(pacienteLookup.buscar(pacienteId)).thenThrow(new ResourceNotFoundException("Paciente", pacienteId));

        AgendamentoRequest request = request(pacienteId, profissionalId, setorId, HORARIO_BASE, null);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any(Agendamento.class));
        verify(profissionalValidation, never()).validar(any());
    }

    @Test
    void criar_deveValidarConvenio_quandoInformado() {
        Long pacienteId = 1L, profissionalId = 2L, setorId = 3L, convenioId = 9L;
        stubDependenciasBasicas(pacienteId, profissionalId, setorId);
        when(convenioLookup.buscar(convenioId)).thenReturn(new ConvenioDto(convenioId, "Unimed"));
        when(repository.findByProfissionalIdAndStatusInAndDataHoraBetween(
                anyLong(), anyCollection(), any(), any())).thenReturn(List.of());
        when(repository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Agendamento.class))).thenReturn(null);

        AgendamentoRequest request = new AgendamentoRequest(pacienteId, profissionalId, setorId, convenioId,
                TipoAgendamento.CONSULTA, HORARIO_BASE, null, null);

        service.criar(request);

        ArgumentCaptor<Agendamento> captor = ArgumentCaptor.forClass(Agendamento.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getConvenioId()).isEqualTo(convenioId);
        assertThat(captor.getValue().getConvenioNome()).isEqualTo("Unimed");
    }

    // ---------- atualizar ----------

    @Test
    void atualizar_deveLancarBusinessException_quandoAgendamentoEncerrado() {
        Long id = 10L;
        Agendamento cancelado = agendamentoExistente(id, 2L, HORARIO_BASE, 30, StatusAgendamento.CANCELADO);
        when(repository.findById(id)).thenReturn(Optional.of(cancelado));

        AgendamentoRequest request = request(1L, 2L, 3L, HORARIO_BASE, null);

        assertThatThrownBy(() -> service.atualizar(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não é possível reagendar");

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void atualizar_naoDeveConsiderarOProprioAgendamentoComoConflito() {
        Long id = 10L;
        Long profissionalId = 2L, pacienteId = 1L, setorId = 3L;
        Agendamento existente = agendamentoExistente(id, profissionalId, HORARIO_BASE, 30, StatusAgendamento.AGENDADO);
        when(repository.findById(id)).thenReturn(Optional.of(existente));
        stubDependenciasBasicas(pacienteId, profissionalId, setorId);
        // O único agendamento do dia é ele mesmo — não deve gerar conflito.
        when(repository.findByProfissionalIdAndStatusInAndDataHoraBetween(
                anyLong(), anyCollection(), any(), any())).thenReturn(List.of(existente));
        when(repository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Agendamento.class))).thenReturn(null);

        // Reagenda para o mesmo horário (mesmo registro) — sem conflito real.
        AgendamentoRequest request = request(pacienteId, profissionalId, setorId, HORARIO_BASE, 30);

        service.atualizar(id, request);

        verify(repository).save(existente);
    }

    @Test
    void atualizar_deveLancarBusinessException_quandoConflitaComOutroAgendamento() {
        Long id = 10L;
        Long profissionalId = 2L, pacienteId = 1L, setorId = 3L;
        Agendamento agendamentoAtual = agendamentoExistente(id, profissionalId, HORARIO_BASE, 30, StatusAgendamento.AGENDADO);
        when(repository.findById(id)).thenReturn(Optional.of(agendamentoAtual));
        stubDependenciasBasicas(pacienteId, profissionalId, setorId);

        Agendamento outro = agendamentoExistente(99L, profissionalId, HORARIO_BASE.plusMinutes(15), 30,
                StatusAgendamento.CONFIRMADO);
        when(repository.findByProfissionalIdAndStatusInAndDataHoraBetween(
                anyLong(), anyCollection(), any(), any())).thenReturn(List.of(agendamentoAtual, outro));

        AgendamentoRequest request = request(pacienteId, profissionalId, setorId, HORARIO_BASE, 30);

        assertThatThrownBy(() -> service.atualizar(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já possui um agendamento");

        verify(repository, never()).save(any(Agendamento.class));
    }

    // ---------- atualizarStatus ----------

    @Test
    void atualizarStatus_deveCancelar_quandoAgendamentoAtivo() {
        Long id = 20L;
        Agendamento agendamento = agendamentoExistente(id, 2L, HORARIO_BASE, 30, StatusAgendamento.AGENDADO);
        when(repository.findById(id)).thenReturn(Optional.of(agendamento));
        when(repository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));
        AgendamentoResponse resposta = new AgendamentoResponse(
                id, 100L, "Paciente 100", 2L, "Dr. Fulano", 200L, "Ambulatório", null, null,
                TipoAgendamento.CONSULTA, StatusAgendamento.CANCELADO, HORARIO_BASE, 30, null, null, null, null);
        when(mapper.toResponse(any(Agendamento.class))).thenReturn(resposta);

        AgendamentoResponse resultado = service.atualizarStatus(id, new AgendamentoStatusRequest(StatusAgendamento.CANCELADO));

        assertThat(resultado.status()).isEqualTo(StatusAgendamento.CANCELADO);
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        verify(repository).save(agendamento);
    }

    @Test
    void atualizarStatus_deveLancarBusinessException_quandoTentaMarcarRealizadoDiretamente() {
        Long id = 20L;
        Agendamento agendamento = agendamentoExistente(id, 2L, HORARIO_BASE, 30, StatusAgendamento.AGENDADO);
        when(repository.findById(id)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.atualizarStatus(id, new AgendamentoStatusRequest(StatusAgendamento.REALIZADO)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Use o check-in");

        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void atualizarStatus_deveLancarBusinessException_quandoAgendamentoJaEncerrado() {
        Long id = 20L;
        Agendamento agendamento = agendamentoExistente(id, 2L, HORARIO_BASE, 30, StatusAgendamento.FALTOU);
        when(repository.findById(id)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.atualizarStatus(id, new AgendamentoStatusRequest(StatusAgendamento.CANCELADO)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não admite nova transição");

        verify(repository, never()).save(any(Agendamento.class));
    }

    // ---------- checkin ----------

    @Test
    void checkin_deveGerarAtendimento_quandoStatusPermiteCheckin() {
        Long id = 30L;
        Agendamento agendamento = agendamentoExistente(id, 2L, HORARIO_BASE, 30, StatusAgendamento.AGENDADO);
        agendamento.setObservacoes("Dor de cabeça");
        when(repository.findById(id)).thenReturn(Optional.of(agendamento));
        when(atendimentoRepository.save(any(Atendimento.class))).thenAnswer(inv -> {
            Atendimento a = inv.getArgument(0);
            a.setId(500L);
            return a;
        });
        when(repository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));
        AgendamentoResponse resposta = new AgendamentoResponse(
                id, 100L, "Paciente 100", 2L, "Dr. Fulano", 200L, "Ambulatório", null, null,
                TipoAgendamento.CONSULTA, StatusAgendamento.REALIZADO, HORARIO_BASE, 30,
                "Dor de cabeça", 500L, null, null);
        when(mapper.toResponse(any(Agendamento.class))).thenReturn(resposta);

        AgendamentoResponse resultado = service.checkin(id);

        assertThat(resultado.status()).isEqualTo(StatusAgendamento.REALIZADO);
        assertThat(resultado.atendimentoId()).isEqualTo(500L);
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.REALIZADO);
        assertThat(agendamento.getAtendimento()).isNotNull();

        ArgumentCaptor<Atendimento> atendimentoCaptor = ArgumentCaptor.forClass(Atendimento.class);
        verify(atendimentoRepository).save(atendimentoCaptor.capture());
        Atendimento atendimentoCriado = atendimentoCaptor.getValue();
        assertThat(atendimentoCriado.getPaciente().getId()).isEqualTo(agendamento.getPacienteId());
        assertThat(atendimentoCriado.getSetor().getId()).isEqualTo(agendamento.getSetorId());
        assertThat(atendimentoCriado.getProfissionalResponsavel().getId()).isEqualTo(agendamento.getProfissionalId());
        assertThat(atendimentoCriado.getQueixaPrincipal()).isEqualTo("Dor de cabeça");
        assertThat(atendimentoCriado.getStatus()).isEqualTo(StatusAtendimento.AGUARDANDO_ATENDIMENTO);
        assertThat(atendimentoCriado.getDataEntrada()).isNotNull();

        verify(repository).save(agendamento);
    }

    @Test
    void checkin_deveLancarBusinessException_quandoStatusNaoPermiteCheckin() {
        Long id = 30L;
        Agendamento agendamento = agendamentoExistente(id, 2L, HORARIO_BASE, 30, StatusAgendamento.CANCELADO);
        when(repository.findById(id)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.checkin(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Check-in indisponível");

        verify(atendimentoRepository, never()).save(any(Atendimento.class));
        verify(repository, never()).save(any(Agendamento.class));
    }

    @Test
    void checkin_deveLancarBusinessException_quandoJaRealizado() {
        Long id = 30L;
        Agendamento agendamento = agendamentoExistente(id, 2L, HORARIO_BASE, 30, StatusAgendamento.AGENDADO);
        Atendimento atendimentoExistente = Atendimento.builder().build();
        atendimentoExistente.setId(999L);
        agendamento.setAtendimento(atendimentoExistente);
        when(repository.findById(id)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.checkin(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Check-in já realizado");

        verify(atendimentoRepository, never()).save(any(Atendimento.class));
        verify(repository, never()).save(any(Agendamento.class));
    }

    // ---------- buscarPorId ----------

    @Test
    void buscarPorId_deveLancarResourceNotFoundException_quandoNaoEncontrado() {
        Long id = 404L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
