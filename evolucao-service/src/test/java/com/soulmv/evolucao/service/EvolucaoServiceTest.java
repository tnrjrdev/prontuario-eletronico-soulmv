package com.soulmv.evolucao.service;

import com.soulmv.evolucao.client.AtendimentoDto;
import com.soulmv.evolucao.dto.request.EvolucaoRequest;
import com.soulmv.evolucao.entity.EvolucaoClinica;
import com.soulmv.evolucao.enums.StatusAtendimento;
import com.soulmv.evolucao.enums.TipoEvolucao;
import com.soulmv.evolucao.exception.BusinessException;
import com.soulmv.evolucao.mapper.EvolucaoMapper;
import com.soulmv.evolucao.repository.EvolucaoClinicaRepository;
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
class EvolucaoServiceTest {

    @Mock EvolucaoClinicaRepository repository;
    @Mock AtendimentoLookupService atendimentoLookup;
    @Mock EvolucaoMapper mapper;

    @InjectMocks EvolucaoService service;

    @Test
    void registrar_deveSalvarEvolucao_comTipoEAutorInformadosPeloControlador() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.EM_ATENDIMENTO));
        when(repository.save(any(EvolucaoClinica.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(EvolucaoClinica.class))).thenReturn(null);

        service.registrar(10L, new EvolucaoRequest("Paciente estável."), 7L, "Dr. Fulano", TipoEvolucao.MEDICA);

        ArgumentCaptor<EvolucaoClinica> captor = ArgumentCaptor.forClass(EvolucaoClinica.class);
        verify(repository).save(captor.capture());
        EvolucaoClinica salva = captor.getValue();
        assertThat(salva.getAtendimentoId()).isEqualTo(10L);
        assertThat(salva.getAutorId()).isEqualTo(7L);
        assertThat(salva.getAutorNome()).isEqualTo("Dr. Fulano");
        assertThat(salva.getTipo()).isEqualTo(TipoEvolucao.MEDICA);
        assertThat(salva.getTexto()).isEqualTo("Paciente estável.");
        assertThat(salva.getDataHora()).isNotNull();
    }

    @Test
    void registrar_deveFalhar_quandoAtendimentoEncerrado() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.CANCELADO));

        assertThatThrownBy(() -> service.registrar(10L, new EvolucaoRequest("texto"), 7L, "Dr. Fulano", TipoEvolucao.MEDICA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrado");

        verify(repository, never()).save(any());
    }

    @Test
    void listar_deveDelegarParaORepositorioOrdenadoPorDataDesc() {
        when(repository.findByAtendimentoIdOrderByDataHoraDesc(10L)).thenReturn(List.of());

        List<?> resultado = service.listar(10L);

        assertThat(resultado).isEmpty();
        verify(repository).findByAtendimentoIdOrderByDataHoraDesc(10L);
    }
}
