package com.soulmv.atendimento.service;

import com.soulmv.atendimento.client.LeitoDto;
import com.soulmv.atendimento.client.PacienteDto;
import com.soulmv.atendimento.client.SetorDto;
import com.soulmv.atendimento.dto.request.AlocarLeitoRequest;
import com.soulmv.atendimento.dto.request.AtendimentoRequest;
import com.soulmv.atendimento.dto.request.AtendimentoStatusRequest;
import com.soulmv.atendimento.entity.Atendimento;
import com.soulmv.atendimento.enums.StatusAtendimento;
import com.soulmv.atendimento.enums.StatusLeito;
import com.soulmv.atendimento.enums.TipoAtendimento;
import com.soulmv.atendimento.exception.BusinessException;
import com.soulmv.atendimento.exception.ResourceNotFoundException;
import com.soulmv.atendimento.mapper.AtendimentoMapper;
import com.soulmv.atendimento.repository.AtendimentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {

    @Mock AtendimentoRepository repository;
    @Mock PacienteLookupService pacienteLookup;
    @Mock SetorLookupService setorLookup;
    @Mock LeitoLookupService leitoLookup;
    @Mock AtendimentoMapper mapper;

    @InjectMocks AtendimentoService service;

    private Atendimento atendimento(Long id, StatusAtendimento status) {
        Atendimento a = Atendimento.builder()
                .pacienteId(1L).pacienteNome("Fulano")
                .setorId(2L).setorNome("Emergência")
                .tipo(TipoAtendimento.EMERGENCIA)
                .status(status)
                .dataEntrada(java.time.LocalDateTime.now())
                .build();
        a.setId(id);
        return a;
    }

    // ---------------------------------------------------------------- abrir

    @Test
    void abrir_deveCriarAtendimento_comDadosDoPacienteESetor() {
        when(pacienteLookup.buscar(1L)).thenReturn(new PacienteDto(1L, "Fulano de Tal"));
        when(setorLookup.buscar(2L)).thenReturn(new SetorDto(2L, "Emergência"));
        when(repository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Atendimento.class))).thenReturn(null);

        service.abrir(new AtendimentoRequest(1L, TipoAtendimento.EMERGENCIA, 2L, "Dor no peito"));

        ArgumentCaptor<Atendimento> captor = ArgumentCaptor.forClass(Atendimento.class);
        verify(repository).save(captor.capture());
        Atendimento salvo = captor.getValue();
        assertThat(salvo.getPacienteNome()).isEqualTo("Fulano de Tal");
        assertThat(salvo.getSetorNome()).isEqualTo("Emergência");
        assertThat(salvo.getStatus()).isEqualTo(StatusAtendimento.AGUARDANDO_TRIAGEM);
        assertThat(salvo.getDataEntrada()).isNotNull();
    }

    @Test
    void abrir_devePropagarResourceNotFoundException_quandoPacienteNaoExiste() {
        when(pacienteLookup.buscar(999L)).thenThrow(new ResourceNotFoundException("Paciente", 999L));

        assertThatThrownBy(() -> service.abrir(new AtendimentoRequest(999L, TipoAtendimento.AMBULATORIAL, 2L, null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
        verify(setorLookup, never()).buscar(any());
    }

    // ---------------------------------------------------------- atualizarStatus

    @Test
    void atualizarStatus_deveFalhar_quandoAtendimentoEncerrado() {
        Atendimento at = atendimento(1L, StatusAtendimento.ALTA);
        when(repository.findById(1L)).thenReturn(Optional.of(at));

        assertThatThrownBy(() -> service.atualizarStatus(1L,
                new AtendimentoStatusRequest(StatusAtendimento.EM_ATENDIMENTO), 10L, "Dra. Ciclana"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrado");

        verify(repository, never()).save(any());
    }

    @Test
    void atualizarStatus_deveRejeitarAlta_direto() {
        Atendimento at = atendimento(1L, StatusAtendimento.EM_ATENDIMENTO);
        when(repository.findById(1L)).thenReturn(Optional.of(at));

        assertThatThrownBy(() -> service.atualizarStatus(1L,
                new AtendimentoStatusRequest(StatusAtendimento.ALTA), 10L, "Dra. Ciclana"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("endpoint de alta");
    }

    @Test
    void atualizarStatus_deveAtribuirProfissionalAtual_quandoEntraEmAtendimento() {
        Atendimento at = atendimento(1L, StatusAtendimento.AGUARDANDO_ATENDIMENTO);
        when(repository.findById(1L)).thenReturn(Optional.of(at));
        when(repository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Atendimento.class))).thenReturn(null);

        service.atualizarStatus(1L, new AtendimentoStatusRequest(StatusAtendimento.EM_ATENDIMENTO), 10L, "Dra. Ciclana");

        assertThat(at.getProfissionalResponsavelId()).isEqualTo(10L);
        assertThat(at.getProfissionalResponsavelNome()).isEqualTo("Dra. Ciclana");
    }

    @Test
    void atualizarStatus_naoDeveSobrescreverProfissional_seJaAtribuido() {
        Atendimento at = atendimento(1L, StatusAtendimento.AGUARDANDO_ATENDIMENTO);
        at.setProfissionalResponsavelId(5L);
        at.setProfissionalResponsavelNome("Dr. Original");
        when(repository.findById(1L)).thenReturn(Optional.of(at));
        when(repository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Atendimento.class))).thenReturn(null);

        service.atualizarStatus(1L, new AtendimentoStatusRequest(StatusAtendimento.EM_ATENDIMENTO), 10L, "Dra. Ciclana");

        assertThat(at.getProfissionalResponsavelId()).isEqualTo(5L);
        assertThat(at.getProfissionalResponsavelNome()).isEqualTo("Dr. Original");
    }

    @Test
    void atualizarStatus_deveLiberarLeito_quandoCancelado() {
        Atendimento at = atendimento(1L, StatusAtendimento.INTERNADO);
        at.setLeitoId(7L);
        at.setLeitoIdentificador("UTI-01");
        when(repository.findById(1L)).thenReturn(Optional.of(at));
        when(repository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Atendimento.class))).thenReturn(null);

        service.atualizarStatus(1L, new AtendimentoStatusRequest(StatusAtendimento.CANCELADO), 10L, "Dra. Ciclana");

        verify(leitoLookup).atualizarStatus(7L, StatusLeito.HIGIENIZACAO);
        assertThat(at.getLeitoId()).isNull();
        assertThat(at.getStatus()).isEqualTo(StatusAtendimento.CANCELADO);
    }

    // -------------------------------------------------------------- alocarLeito

    @Test
    void alocarLeito_deveInternar_eLiberarLeitoAnteriorSoDepoisDoNovoConfirmado() {
        Atendimento at = atendimento(1L, StatusAtendimento.EM_ATENDIMENTO);
        at.setLeitoId(3L);
        at.setLeitoIdentificador("ENF-02");
        when(repository.findById(1L)).thenReturn(Optional.of(at));
        when(leitoLookup.atualizarStatus(9L, StatusLeito.OCUPADO)).thenReturn(new LeitoDto(9L, "UTI-05", true));
        when(repository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Atendimento.class))).thenReturn(null);

        service.alocarLeito(1L, new AlocarLeitoRequest(9L));

        assertThat(at.getLeitoId()).isEqualTo(9L);
        assertThat(at.getLeitoIdentificador()).isEqualTo("UTI-05");
        assertThat(at.getStatus()).isEqualTo(StatusAtendimento.INTERNADO);
        verify(leitoLookup).atualizarStatus(3L, StatusLeito.HIGIENIZACAO);
    }

    @Test
    void alocarLeito_deveFalhar_quandoAtendimentoEncerrado() {
        Atendimento at = atendimento(1L, StatusAtendimento.ALTA);
        when(repository.findById(1L)).thenReturn(Optional.of(at));

        assertThatThrownBy(() -> service.alocarLeito(1L, new AlocarLeitoRequest(9L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrado");

        verify(leitoLookup, never()).atualizarStatus(any(), any());
    }

    @Test
    void alocarLeito_devePropagarErroDoLeito_semTocarNoAntigo() {
        Atendimento at = atendimento(1L, StatusAtendimento.EM_ATENDIMENTO);
        at.setLeitoId(3L);
        at.setLeitoIdentificador("ENF-02");
        when(repository.findById(1L)).thenReturn(Optional.of(at));
        when(leitoLookup.atualizarStatus(9L, StatusLeito.OCUPADO))
                .thenThrow(new BusinessException("Leito indisponível para esta operação."));

        assertThatThrownBy(() -> service.alocarLeito(1L, new AlocarLeitoRequest(9L)))
                .isInstanceOf(BusinessException.class);

        // O leito antigo não pode ter sido liberado, já que o novo falhou.
        verify(leitoLookup, never()).atualizarStatus(eq(3L), any());
        assertThat(at.getLeitoId()).isEqualTo(3L);
        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------------ darAlta

    @Test
    void darAlta_deveEncerrarAtendimento_eLiberarLeito() {
        Atendimento at = atendimento(1L, StatusAtendimento.INTERNADO);
        at.setLeitoId(7L);
        at.setLeitoIdentificador("UTI-01");
        when(repository.findById(1L)).thenReturn(Optional.of(at));
        when(repository.save(any(Atendimento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Atendimento.class))).thenReturn(null);

        service.darAlta(1L, 10L, "Dr. Medico");

        assertThat(at.getStatus()).isEqualTo(StatusAtendimento.ALTA);
        assertThat(at.getDataAlta()).isNotNull();
        assertThat(at.getLeitoId()).isNull();
        verify(leitoLookup).atualizarStatus(7L, StatusLeito.HIGIENIZACAO);
    }

    @Test
    void darAlta_deveFalhar_quandoJaEncerrado() {
        Atendimento at = atendimento(1L, StatusAtendimento.CANCELADO);
        when(repository.findById(1L)).thenReturn(Optional.of(at));

        assertThatThrownBy(() -> service.darAlta(1L, 10L, "Dr. Medico"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrado");
    }

    // -------------------------------------------------------------- buscarPorId

    @Test
    void buscarPorId_deveFalhar_quandoNaoExiste() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(404L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
