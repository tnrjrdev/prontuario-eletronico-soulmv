package com.soulmv.agendamento.service;

import com.soulmv.agendamento.client.ConvenioClient;
import com.soulmv.agendamento.client.ConvenioDto;
import com.soulmv.agendamento.exception.BusinessException;
import com.soulmv.agendamento.exception.ResourceNotFoundException;
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
class ConvenioLookupServiceTest {

    @Mock ConvenioClient client;
    @InjectMocks ConvenioLookupService service;

    @Test
    void buscar_deveDelegarParaOClienteFeign() {
        ConvenioDto dto = new ConvenioDto(9L, "Unimed");
        when(client.buscarPorId(9L)).thenReturn(dto);

        assertThat(service.buscar(9L)).isSameAs(dto);
    }

    @Test
    void buscar_deveTraduzir404DoFeignEmResourceNotFoundException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/convenios/999",
                Map.of(), null, new RequestTemplate());
        when(client.buscarPorId(999L)).thenThrow(new FeignException.NotFound("not found", request, null, null));

        assertThatThrownBy(() -> service.buscar(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void fallback_deveRecusarAOperacao_quandoCatalogoIndisponivel() {
        assertThatThrownBy(() -> service.fallbackBuscar(9L, new RuntimeException("timeout")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }
}
