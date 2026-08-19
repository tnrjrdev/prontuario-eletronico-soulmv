package com.soulmv.agendamento.service;

import com.soulmv.agendamento.client.PacienteClient;
import com.soulmv.agendamento.client.PacienteDto;
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
class PacienteLookupServiceTest {

    @Mock PacienteClient client;
    @InjectMocks PacienteLookupService service;

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/pacientes/999",
                Map.of(), null, new RequestTemplate());
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    void buscar_deveDelegarParaOClienteFeign() {
        PacienteDto dto = new PacienteDto(1L, "Fulano de Tal");
        when(client.buscarPorId(1L)).thenReturn(dto);

        assertThat(service.buscar(1L)).isSameAs(dto);
    }

    @Test
    void buscar_deveTraduzir404DoFeignEmResourceNotFoundException() {
        when(client.buscarPorId(999L)).thenThrow(notFound());

        assertThatThrownBy(() -> service.buscar(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void fallback_deveRecusarAOperacao_quandoPacienteServiceIndisponivel() {
        assertThatThrownBy(() -> service.fallbackBuscar(1L, new RuntimeException("timeout")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }
}
