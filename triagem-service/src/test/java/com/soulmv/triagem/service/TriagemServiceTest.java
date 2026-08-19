package com.soulmv.triagem.service;

import com.soulmv.triagem.client.AtendimentoDto;
import com.soulmv.triagem.dto.request.TriagemRequest;
import com.soulmv.triagem.entity.Triagem;
import com.soulmv.triagem.enums.ClassificacaoRisco;
import com.soulmv.triagem.enums.StatusAtendimento;
import com.soulmv.triagem.exception.BusinessException;
import com.soulmv.triagem.exception.ResourceNotFoundException;
import com.soulmv.triagem.mapper.TriagemMapper;
import com.soulmv.triagem.repository.TriagemRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriagemServiceTest {

    @Mock TriagemRepository repository;
    @Mock AtendimentoLookupService atendimentoLookup;
    @Mock TriagemMapper mapper;

    @InjectMocks TriagemService service;

    @Test
    void registrar_deveSalvarTriagem_eAvancarStatusDoAtendimento() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.AGUARDANDO_TRIAGEM));
        when(repository.existsByAtendimentoId(10L)).thenReturn(false);
        when(repository.save(any(Triagem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Triagem.class))).thenReturn(null);

        service.registrar(10L, new TriagemRequest(ClassificacaoRisco.LARANJA, "Dor intensa"), 5L, "Enf. Ciclana");

        ArgumentCaptor<Triagem> captor = ArgumentCaptor.forClass(Triagem.class);
        verify(repository).save(captor.capture());
        Triagem salva = captor.getValue();
        assertThat(salva.getAtendimentoId()).isEqualTo(10L);
        assertThat(salva.getEnfermeiroId()).isEqualTo(5L);
        assertThat(salva.getEnfermeiroNome()).isEqualTo("Enf. Ciclana");
        assertThat(salva.getClassificacaoRisco()).isEqualTo(ClassificacaoRisco.LARANJA);

        verify(atendimentoLookup).atualizarStatus(10L, StatusAtendimento.AGUARDANDO_ATENDIMENTO);
    }

    @Test
    void registrar_naoDeveAvancarStatus_quandoAtendimentoJaEstaAlemDaTriagem() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.EM_ATENDIMENTO));
        when(repository.existsByAtendimentoId(10L)).thenReturn(false);
        when(repository.save(any(Triagem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Triagem.class))).thenReturn(null);

        service.registrar(10L, new TriagemRequest(ClassificacaoRisco.VERDE, null), 5L, "Enf. Ciclana");

        verify(atendimentoLookup, never()).atualizarStatus(any(), any());
    }

    @Test
    void registrar_deveFalhar_quandoAtendimentoEncerrado() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.ALTA));

        assertThatThrownBy(() -> service.registrar(10L, new TriagemRequest(ClassificacaoRisco.VERDE, null), 5L, "Enf. Ciclana"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrado");

        verify(repository, never()).save(any());
    }

    @Test
    void registrar_deveFalhar_quandoJaExisteTriagem() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.AGUARDANDO_TRIAGEM));
        when(repository.existsByAtendimentoId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(10L, new TriagemRequest(ClassificacaoRisco.VERDE, null), 5L, "Enf. Ciclana"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já possui triagem");

        verify(repository, never()).save(any());
    }

    @Test
    void registrar_devePropagarErro_quandoAtendimentoNaoExiste() {
        when(atendimentoLookup.buscar(999L)).thenThrow(new ResourceNotFoundException("Atendimento", 999L));

        assertThatThrownBy(() -> service.registrar(999L, new TriagemRequest(ClassificacaoRisco.VERDE, null), 5L, "Enf. Ciclana"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void buscar_deveFalhar_quandoNaoExisteTriagemParaOAtendimento() {
        when(repository.findByAtendimentoId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(10L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
