package com.soulmv.dashboard.service;

import com.soulmv.dashboard.client.ContaClient;
import com.soulmv.dashboard.client.ContaEstatisticasDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaEstatisticasLookupServiceTest {

    @Mock ContaClient contaClient;
    @InjectMocks ContaEstatisticasLookupService service;

    @Test
    void estatisticas_deveDelegarParaOClienteFeign() {
        ContaEstatisticasDto dto = new ContaEstatisticasDto(5, new BigDecimal("100.00"), Map.of("ABERTA", 5L), Map.of());
        when(contaClient.estatisticas()).thenReturn(dto);

        assertThat(service.estatisticas()).isSameAs(dto);
    }

    @Test
    void fallback_deveDevolverEstatisticasZeradas_semLancarExcecao() {
        ContaEstatisticasDto fallback = service.fallbackEstatisticas(new RuntimeException("faturamento-service fora do ar"));

        assertThat(fallback.total()).isZero();
        assertThat(fallback.valorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(fallback.contasPorStatus()).isEmpty();
        assertThat(fallback.valorPorStatus()).isEmpty();
    }
}
