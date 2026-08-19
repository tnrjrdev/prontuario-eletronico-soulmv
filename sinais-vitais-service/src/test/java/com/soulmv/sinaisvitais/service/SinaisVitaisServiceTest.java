package com.soulmv.sinaisvitais.service;

import com.soulmv.sinaisvitais.client.AtendimentoDto;
import com.soulmv.sinaisvitais.dto.request.SinaisVitaisRequest;
import com.soulmv.sinaisvitais.entity.SinaisVitais;
import com.soulmv.sinaisvitais.enums.StatusAtendimento;
import com.soulmv.sinaisvitais.exception.BusinessException;
import com.soulmv.sinaisvitais.mapper.SinaisVitaisMapper;
import com.soulmv.sinaisvitais.repository.SinaisVitaisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SinaisVitaisServiceTest {

    @Mock SinaisVitaisRepository repository;
    @Mock AtendimentoLookupService atendimentoLookup;
    @Mock SinaisVitaisMapper mapper;

    @InjectMocks SinaisVitaisService service;

    private SinaisVitaisRequest request() {
        return new SinaisVitaisRequest(120, 80, 90, 18, new BigDecimal("36.5"), 98, 95, 3);
    }

    @Test
    void registrar_deveSalvarSinaisVitais_comAutorDenormalizado() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.EM_ATENDIMENTO));
        when(repository.save(any(SinaisVitais.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(SinaisVitais.class))).thenReturn(null);

        service.registrar(10L, request(), 5L, "Enf. Ciclana");

        ArgumentCaptor<SinaisVitais> captor = ArgumentCaptor.forClass(SinaisVitais.class);
        verify(repository).save(captor.capture());
        SinaisVitais salvo = captor.getValue();
        assertThat(salvo.getAtendimentoId()).isEqualTo(10L);
        assertThat(salvo.getRegistradoPorId()).isEqualTo(5L);
        assertThat(salvo.getRegistradoPorNome()).isEqualTo("Enf. Ciclana");
        assertThat(salvo.getPressaoSistolica()).isEqualTo(120);
        assertThat(salvo.getDataHora()).isNotNull();
    }

    @Test
    void registrar_deveFalhar_quandoAtendimentoEncerrado() {
        when(atendimentoLookup.buscar(10L)).thenReturn(new AtendimentoDto(10L, StatusAtendimento.ALTA));

        assertThatThrownBy(() -> service.registrar(10L, request(), 5L, "Enf. Ciclana"))
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
