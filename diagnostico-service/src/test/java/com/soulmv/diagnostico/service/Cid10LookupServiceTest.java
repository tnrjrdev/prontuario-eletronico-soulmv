package com.soulmv.diagnostico.service;

import com.soulmv.diagnostico.client.Cid10Client;
import com.soulmv.diagnostico.client.Cid10Dto;
import com.soulmv.diagnostico.exception.BusinessException;
import com.soulmv.diagnostico.exception.ResourceNotFoundException;
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
class Cid10LookupServiceTest {

    @Mock Cid10Client client;
    @InjectMocks Cid10LookupService service;

    @Test
    void buscar_deveDelegarParaOClienteFeign() {
        Cid10Dto dto = new Cid10Dto(5L, "J11", "Influenza");
        when(client.buscarPorId(5L)).thenReturn(dto);

        assertThat(service.buscar(5L)).isSameAs(dto);
    }

    @Test
    void buscar_deveTraduzir404EmResourceNotFoundException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/cid10/999",
                Map.of(), null, new RequestTemplate());
        when(client.buscarPorId(999L)).thenThrow(new FeignException.NotFound("not found", request, null, null));

        assertThatThrownBy(() -> service.buscar(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void fallback_deveRecusarAOperacao_quandoCatalogoIndisponivel() {
        assertThatThrownBy(() -> service.fallbackBuscar(5L, new RuntimeException("timeout")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }
}
