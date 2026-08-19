package com.soulmv.dashboard.service;

import com.soulmv.dashboard.client.LeitoClient;
import com.soulmv.dashboard.client.LeitoEstatisticasDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeitoEstatisticasLookupServiceTest {

    @Mock LeitoClient leitoClient;
    @InjectMocks LeitoEstatisticasLookupService service;

    @Test
    void estatisticas_deveDelegarParaOClienteFeign() {
        LeitoEstatisticasDto dto = new LeitoEstatisticasDto(10, 8, 4, 4, Map.of("LIVRE", 4L));
        when(leitoClient.estatisticas()).thenReturn(dto);

        assertThat(service.estatisticas()).isSameAs(dto);
    }

    @Test
    void fallback_deveDevolverEstatisticasZeradas_semLancarExcecao() {
        LeitoEstatisticasDto fallback = service.fallbackEstatisticas(new RuntimeException("catalogo-service fora do ar"));

        assertThat(fallback.total()).isZero();
        assertThat(fallback.ativos()).isZero();
        assertThat(fallback.ocupados()).isZero();
        assertThat(fallback.livres()).isZero();
        assertThat(fallback.porStatus()).isEmpty();
    }
}
