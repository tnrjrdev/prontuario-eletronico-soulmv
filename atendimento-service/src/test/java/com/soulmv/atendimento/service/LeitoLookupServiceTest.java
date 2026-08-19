package com.soulmv.atendimento.service;

import com.soulmv.atendimento.client.LeitoClient;
import com.soulmv.atendimento.client.LeitoDto;
import com.soulmv.atendimento.client.LeitoStatusRequestDto;
import com.soulmv.atendimento.enums.StatusLeito;
import com.soulmv.atendimento.exception.BusinessException;
import com.soulmv.atendimento.exception.ResourceNotFoundException;
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
class LeitoLookupServiceTest {

    @Mock LeitoClient client;
    @InjectMocks LeitoLookupService service;

    private Request request(String path) {
        return Request.create(Request.HttpMethod.PATCH, path, Map.of(), null, new RequestTemplate());
    }

    @Test
    void atualizarStatus_deveDelegarParaOClienteFeign() {
        LeitoDto dto = new LeitoDto(9L, "UTI-05", true);
        when(client.atualizarStatus(9L, new LeitoStatusRequestDto(StatusLeito.OCUPADO))).thenReturn(dto);

        assertThat(service.atualizarStatus(9L, StatusLeito.OCUPADO)).isSameAs(dto);
    }

    @Test
    void atualizarStatus_deveTraduzir404EmResourceNotFoundException() {
        when(client.atualizarStatus(999L, new LeitoStatusRequestDto(StatusLeito.OCUPADO)))
                .thenThrow(new FeignException.NotFound("not found", request("/api/leitos/999/status"), null, null));

        assertThatThrownBy(() -> service.atualizarStatus(999L, StatusLeito.OCUPADO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void atualizarStatus_deveTraduzir409EmBusinessExceptionDeIndisponibilidade() {
        when(client.atualizarStatus(9L, new LeitoStatusRequestDto(StatusLeito.OCUPADO)))
                .thenThrow(new FeignException.Conflict("conflict", request("/api/leitos/9/status"), null, null));

        assertThatThrownBy(() -> service.atualizarStatus(9L, StatusLeito.OCUPADO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void fallback_deveRecusarAOperacao_quandoCatalogoIndisponivel() {
        assertThatThrownBy(() -> service.fallbackAtualizarStatus(9L, StatusLeito.OCUPADO, new RuntimeException("timeout")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }
}
