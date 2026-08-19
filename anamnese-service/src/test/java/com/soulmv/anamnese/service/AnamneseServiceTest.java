package com.soulmv.anamnese.service;

import com.soulmv.anamnese.client.AtendimentoDto;
import com.soulmv.anamnese.dto.request.AnamneseRequest;
import com.soulmv.anamnese.entity.Anamnese;
import com.soulmv.anamnese.enums.StatusAtendimento;
import com.soulmv.anamnese.exception.BusinessException;
import com.soulmv.anamnese.exception.ResourceNotFoundException;
import com.soulmv.anamnese.mapper.AnamneseMapper;
import com.soulmv.anamnese.repository.AnamneseRepository;
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
class AnamneseServiceTest {

    @Mock AnamneseRepository repository;
    @Mock AtendimentoLookupService atendimentoLookup;
    @Mock AnamneseMapper mapper;

    @InjectMocks AnamneseService service;

    private AnamneseRequest request() {
        return new AnamneseRequest("Febre há 3 dias", "Nenhum", "Dipirona", "Normal");
    }

    @Test
    void registrar_deveSalvarAnamnese_comMedicoDenormalizado() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.EM_ATENDIMENTO));
        when(repository.existsByAtendimentoId(10L)).thenReturn(false);
        when(repository.save(any(Anamnese.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Anamnese.class))).thenReturn(null);

        service.registrar(10L, request(), 7L, "Dr. Fulano");

        ArgumentCaptor<Anamnese> captor = ArgumentCaptor.forClass(Anamnese.class);
        verify(repository).save(captor.capture());
        Anamnese salva = captor.getValue();
        assertThat(salva.getAtendimentoId()).isEqualTo(10L);
        assertThat(salva.getMedicoId()).isEqualTo(7L);
        assertThat(salva.getMedicoNome()).isEqualTo("Dr. Fulano");
        assertThat(salva.getAlergias()).isEqualTo("Dipirona");
        assertThat(salva.getDataHora()).isNotNull();
    }

    @Test
    void registrar_deveFalhar_quandoAtendimentoEncerrado() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.ALTA));

        assertThatThrownBy(() -> service.registrar(10L, request(), 7L, "Dr. Fulano"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrado");

        verify(repository, never()).save(any());
    }

    @Test
    void registrar_deveFalhar_quandoJaExisteAnamnese() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.EM_ATENDIMENTO));
        when(repository.existsByAtendimentoId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(10L, request(), 7L, "Dr. Fulano"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já possui anamnese");

        verify(repository, never()).save(any());
    }

    @Test
    void buscar_deveFalhar_quandoNaoExisteAnamneseParaOAtendimento() {
        when(repository.findByAtendimentoId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(10L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
