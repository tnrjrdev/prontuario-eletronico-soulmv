package com.soulmv.triagem.service;

import com.soulmv.triagem.client.AtendimentoClient;
import com.soulmv.triagem.client.AtendimentoDto;
import com.soulmv.triagem.client.AtendimentoStatusRequestDto;
import com.soulmv.triagem.enums.StatusAtendimento;
import com.soulmv.triagem.exception.BusinessException;
import com.soulmv.triagem.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoLookupServiceTest {

    @Mock AtendimentoClient client;
    @InjectMocks AtendimentoLookupService service;

    @Test
    void buscar_deveDelegarParaOClienteFeign() {
        AtendimentoDto dto = new AtendimentoDto(10L, StatusAtendimento.AGUARDANDO_TRIAGEM);
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
    void atualizarStatus_deveDelegarParaOClienteFeign() {
        service.atualizarStatus(10L, StatusAtendimento.AGUARDANDO_ATENDIMENTO);

        verify(client).atualizarStatus(10L, new AtendimentoStatusRequestDto(StatusAtendimento.AGUARDANDO_ATENDIMENTO));
    }

    @Test
    void fallbackBuscar_deveRecusarAOperacao_quandoAtendimentoServiceIndisponivel() {
        assertThatThrownBy(() -> service.fallbackBuscar(10L, new RuntimeException("timeout")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void fallbackAtualizarStatus_deveRecusarAOperacao_quandoAtendimentoServiceIndisponivel() {
        assertThatThrownBy(() -> service.fallbackAtualizarStatus(10L, StatusAtendimento.AGUARDANDO_ATENDIMENTO,
                new RuntimeException("timeout")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }
}
