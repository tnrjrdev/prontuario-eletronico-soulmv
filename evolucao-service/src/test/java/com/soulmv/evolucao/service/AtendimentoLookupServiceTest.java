package com.soulmv.evolucao.service;

import com.soulmv.evolucao.client.AtendimentoClient;
import com.soulmv.evolucao.client.AtendimentoDto;
import com.soulmv.evolucao.enums.StatusAtendimento;
import com.soulmv.evolucao.exception.BusinessException;
import com.soulmv.evolucao.exception.ResourceNotFoundException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoLookupServiceTest {

    @Mock AtendimentoClient client;
    @InjectMocks AtendimentoLookupService service;

    @Test
    void buscar_deveDelegarParaOClienteFeign() {
        AtendimentoDto dto = new AtendimentoDto(10L, StatusAtendimento.EM_ATENDIMENTO);
        when(client.buscarPorId(10L)).thenReturn(dto);

        assertThat(service.buscar(10L)).isSameAs(dto);
    }

    @Test
    void buscar_deveTraduzir404EmResourceNotFoundException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/atendimentos/999",
                Map.of(), null, new RequestTemplate());
        when(client.buscarPorId(999L)).thenThrow(new FeignException.NotFound("not found", request, null, null));

        assertThatThrownBy(() -> service.buscar(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void fallback_deveRecusarAOperacao_quandoAtendimentoServiceIndisponivel() {
        assertThatThrownBy(() -> service.fallbackBuscar(10L, new RuntimeException("timeout")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }
}
