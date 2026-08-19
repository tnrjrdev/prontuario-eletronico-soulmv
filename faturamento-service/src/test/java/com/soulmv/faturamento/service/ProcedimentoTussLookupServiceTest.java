package com.soulmv.faturamento.service;

import com.soulmv.faturamento.client.ProcedimentoTussClient;
import com.soulmv.faturamento.client.ProcedimentoTussDto;
import com.soulmv.faturamento.exception.BusinessException;
import com.soulmv.faturamento.exception.ResourceNotFoundException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcedimentoTussLookupServiceTest {

    @Mock ProcedimentoTussClient client;
    @InjectMocks ProcedimentoTussLookupService service;

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/procedimentos-tuss/999",
                Map.of(), null, new RequestTemplate());
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    void buscar_deveDelegarParaOClienteFeign() {
        ProcedimentoTussDto dto = new ProcedimentoTussDto(7L, "100", "Consulta", BigDecimal.TEN, true);
        when(client.buscarPorId(7L)).thenReturn(dto);

        assertThat(service.buscar(7L)).isSameAs(dto);
    }

    @Test
    void buscar_deveTraduzir404DoFeignEmResourceNotFoundException() {
        when(client.buscarPorId(999L)).thenThrow(notFound());

        assertThatThrownBy(() -> service.buscar(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Procedimento TUSS");
    }

    @Test
    void fallback_deveRecusarAOperacao_quandoCatalogoIndisponivel() {
        assertThatThrownBy(() -> service.fallbackBuscar(7L, new RuntimeException("timeout")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }
}
