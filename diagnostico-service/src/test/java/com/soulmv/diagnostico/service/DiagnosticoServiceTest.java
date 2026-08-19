package com.soulmv.diagnostico.service;

import com.soulmv.diagnostico.client.AtendimentoDto;
import com.soulmv.diagnostico.client.Cid10Dto;
import com.soulmv.diagnostico.dto.request.DiagnosticoRequest;
import com.soulmv.diagnostico.entity.Diagnostico;
import com.soulmv.diagnostico.enums.StatusAtendimento;
import com.soulmv.diagnostico.enums.TipoDiagnostico;
import com.soulmv.diagnostico.exception.BusinessException;
import com.soulmv.diagnostico.mapper.DiagnosticoMapper;
import com.soulmv.diagnostico.repository.DiagnosticoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosticoServiceTest {

    @Mock DiagnosticoRepository repository;
    @Mock AtendimentoLookupService atendimentoLookup;
    @Mock Cid10LookupService cid10Lookup;
    @Mock DiagnosticoMapper mapper;

    @InjectMocks DiagnosticoService service;

    @Test
    void adicionar_deveSalvarDiagnostico_comCidEMedicoDenormalizados() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.EM_ATENDIMENTO));
        when(cid10Lookup.buscar(5L)).thenReturn(new Cid10Dto(5L, "J11", "Influenza"));
        when(repository.save(any(Diagnostico.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Diagnostico.class))).thenReturn(null);

        service.adicionar(10L, new DiagnosticoRequest(5L, TipoDiagnostico.PRINCIPAL, "obs"), 7L, "Dr. Fulano");

        ArgumentCaptor<Diagnostico> captor = ArgumentCaptor.forClass(Diagnostico.class);
        verify(repository).save(captor.capture());
        Diagnostico salvo = captor.getValue();
        assertThat(salvo.getAtendimentoId()).isEqualTo(10L);
        assertThat(salvo.getCid10Id()).isEqualTo(5L);
        assertThat(salvo.getCid10Codigo()).isEqualTo("J11");
        assertThat(salvo.getMedicoId()).isEqualTo(7L);
        assertThat(salvo.getMedicoNome()).isEqualTo("Dr. Fulano");
        assertThat(salvo.getTipo()).isEqualTo(TipoDiagnostico.PRINCIPAL);
        assertThat(salvo.getDataHora()).isNotNull();
    }

    @Test
    void adicionar_deveFalhar_quandoAtendimentoEncerrado() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.ALTA));

        assertThatThrownBy(() -> service.adicionar(10L, new DiagnosticoRequest(5L, TipoDiagnostico.PRINCIPAL, null), 7L, "Dr. Fulano"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrado");

        verify(repository, never()).save(any());
        verify(cid10Lookup, never()).buscar(any());
    }

    @Test
    void listar_deveDelegarParaORepositorioOrdenadoPorDataDesc() {
        when(repository.findByAtendimentoIdOrderByDataHoraDesc(10L)).thenReturn(List.of());

        List<?> resultado = service.listar(10L);

        assertThat(resultado).isEmpty();
        verify(repository).findByAtendimentoIdOrderByDataHoraDesc(10L);
    }
}
