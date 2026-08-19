package com.soulmv.dashboard.service;

import com.soulmv.dashboard.client.ContaEstatisticasDto;
import com.soulmv.dashboard.client.LeitoEstatisticasDto;
import com.soulmv.dashboard.dto.response.AtendimentosDashboardResponse;
import com.soulmv.dashboard.dto.response.FaturamentoDashboardResponse;
import com.soulmv.dashboard.dto.response.OcupacaoLeitosResponse;
import com.soulmv.dashboard.enums.StatusAtendimento;
import com.soulmv.dashboard.repository.AtendimentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a lógica de cálculo/agregação dos indicadores do DashboardService.
 * Ocupação e faturamento vêm de {@link LeitoEstatisticasLookupService}/
 * {@link ContaEstatisticasLookupService} (Feign para catalogo-service/faturamento-service,
 * já com fallback do circuit breaker — testado nas classes de lookup, não aqui).
 * Atendimentos continua lendo direto o repositório compartilhado (domínio ainda não
 * extraído do monólito).
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private LeitoEstatisticasLookupService leitoLookup;

    @Mock
    private ContaEstatisticasLookupService contaLookup;

    private DashboardService service;

    private void criarService() {
        service = new DashboardService(atendimentoRepository, leitoLookup, contaLookup);
    }

    // ---------------------------------------------------------------
    // ocupacaoLeitos()
    // ---------------------------------------------------------------

    @Test
    void ocupacaoLeitos_deveCalcularTaxaCorretamente() {
        criarService();

        Map<String, Long> porStatus = new LinkedHashMap<>();
        porStatus.put("LIVRE", 30L);
        porStatus.put("OCUPADO", 40L);
        porStatus.put("MANUTENCAO", 5L);
        porStatus.put("HIGIENIZACAO", 3L);
        porStatus.put("INTERDITADO", 2L);
        when(leitoLookup.estatisticas()).thenReturn(new LeitoEstatisticasDto(100, 80, 40, 30, porStatus));

        OcupacaoLeitosResponse resultado = service.ocupacaoLeitos();

        assertThat(resultado.totalLeitos()).isEqualTo(100L);
        assertThat(resultado.leitosAtivos()).isEqualTo(80L);
        assertThat(resultado.ocupados()).isEqualTo(40L);
        assertThat(resultado.livres()).isEqualTo(30L);
        // 40 ocupados / 80 ativos = 50%
        assertThat(resultado.taxaOcupacaoPercent()).isEqualTo(50.0);
        assertThat(resultado.porStatus())
                .containsEntry("LIVRE", 30L)
                .containsEntry("OCUPADO", 40L)
                .containsEntry("MANUTENCAO", 5L)
                .containsEntry("HIGIENIZACAO", 3L)
                .containsEntry("INTERDITADO", 2L);
    }

    @Test
    void ocupacaoLeitos_deveArredondarTaxaComDuasCasasHalfUp() {
        criarService();

        // 1 ocupado em 3 ativos = 33.333...% -> deve arredondar para 33.33
        when(leitoLookup.estatisticas()).thenReturn(new LeitoEstatisticasDto(3, 3, 1, 2, Map.of()));

        OcupacaoLeitosResponse resultado = service.ocupacaoLeitos();

        assertThat(resultado.taxaOcupacaoPercent()).isEqualTo(33.33);
    }

    @Test
    void ocupacaoLeitos_semLeitosAtivos_naoDeveGerarExcecaoNemNaN() {
        criarService();

        when(leitoLookup.estatisticas()).thenReturn(new LeitoEstatisticasDto(0, 0, 0, 0, Map.of()));

        OcupacaoLeitosResponse resultado = service.ocupacaoLeitos();

        // Caso de borda: sem leitos ativos, a taxa deve ser 0.0 (código protege com
        // "ativos > 0 ? ... : 0.0"), sem divisão por zero nem NaN silencioso.
        assertThat(resultado.taxaOcupacaoPercent()).isEqualTo(0.0);
        assertThat(resultado.taxaOcupacaoPercent()).isNotNaN();
        assertThat(resultado.totalLeitos()).isZero();
    }

    @Test
    void ocupacaoLeitos_quandoCatalogoIndisponivel_deveUsarFallbackSemQuebrar() {
        criarService();

        // O fallback do circuit breaker (LeitoEstatisticasLookupService) já degrada para
        // zeros; aqui só confirmamos que o DashboardService não lança exceção nesse caso.
        when(leitoLookup.estatisticas()).thenReturn(new LeitoEstatisticasDto(0, 0, 0, 0, Map.of()));

        OcupacaoLeitosResponse resultado = service.ocupacaoLeitos();

        assertThat(resultado.taxaOcupacaoPercent()).isEqualTo(0.0);
    }

    // ---------------------------------------------------------------
    // atendimentos()
    // ---------------------------------------------------------------

    @Test
    void atendimentos_deveAgregarTotalEContagemPorStatus() {
        criarService();

        when(atendimentoRepository.count()).thenReturn(42L);

        Map<StatusAtendimento, Long> contagem = new EnumMap<>(StatusAtendimento.class);
        contagem.put(StatusAtendimento.AGUARDANDO_TRIAGEM, 5L);
        contagem.put(StatusAtendimento.EM_TRIAGEM, 3L);
        contagem.put(StatusAtendimento.AGUARDANDO_ATENDIMENTO, 4L);
        contagem.put(StatusAtendimento.EM_ATENDIMENTO, 10L);
        contagem.put(StatusAtendimento.INTERNADO, 8L);
        contagem.put(StatusAtendimento.AGUARDANDO_EXAME, 2L);
        contagem.put(StatusAtendimento.ALTA, 9L);
        contagem.put(StatusAtendimento.CANCELADO, 1L);
        when(atendimentoRepository.countByStatus(any(StatusAtendimento.class)))
                .thenAnswer(inv -> contagem.get(inv.getArgument(0, StatusAtendimento.class)));

        AtendimentosDashboardResponse resultado = service.atendimentos();

        assertThat(resultado.total()).isEqualTo(42L);
        assertThat(resultado.porStatus())
                .containsEntry("AGUARDANDO_TRIAGEM", 5L)
                .containsEntry("EM_TRIAGEM", 3L)
                .containsEntry("AGUARDANDO_ATENDIMENTO", 4L)
                .containsEntry("EM_ATENDIMENTO", 10L)
                .containsEntry("INTERNADO", 8L)
                .containsEntry("AGUARDANDO_EXAME", 2L)
                .containsEntry("ALTA", 9L)
                .containsEntry("CANCELADO", 1L);
        assertThat(resultado.porStatus().values().stream().mapToLong(Long::longValue).sum())
                .isEqualTo(42L);
    }

    @Test
    void atendimentos_semRegistros_deveRetornarTotaisZerados() {
        criarService();

        when(atendimentoRepository.count()).thenReturn(0L);
        when(atendimentoRepository.countByStatus(any(StatusAtendimento.class))).thenReturn(0L);

        AtendimentosDashboardResponse resultado = service.atendimentos();

        assertThat(resultado.total()).isZero();
        assertThat(resultado.porStatus().values()).allMatch(v -> v == 0L);
        assertThat(resultado.porStatus()).hasSize(StatusAtendimento.values().length);
    }

    // ---------------------------------------------------------------
    // faturamento()
    // ---------------------------------------------------------------

    @Test
    void faturamento_deveSomarValoresPorStatusEValorGeral() {
        criarService();

        Map<String, Long> contagem = new LinkedHashMap<>();
        contagem.put("ABERTA", 4L);
        contagem.put("FECHADA", 2L);
        contagem.put("FATURADA", 3L);
        contagem.put("GLOSADA", 1L);
        contagem.put("CANCELADA", 0L);

        Map<String, BigDecimal> somas = new LinkedHashMap<>();
        somas.put("ABERTA", new BigDecimal("4000.00"));
        somas.put("FECHADA", new BigDecimal("2000.00"));
        somas.put("FATURADA", new BigDecimal("9000.00"));
        somas.put("GLOSADA", new BigDecimal("0.00"));
        somas.put("CANCELADA", BigDecimal.ZERO);

        when(contaLookup.estatisticas()).thenReturn(
                new ContaEstatisticasDto(10, new BigDecimal("15000.00"), contagem, somas));

        FaturamentoDashboardResponse resultado = service.faturamento();

        assertThat(resultado.totalContas()).isEqualTo(10L);
        assertThat(resultado.valorTotalGeral()).isEqualByComparingTo("15000.00");
        assertThat(resultado.contasPorStatus())
                .containsEntry("ABERTA", 4L)
                .containsEntry("FECHADA", 2L)
                .containsEntry("FATURADA", 3L)
                .containsEntry("GLOSADA", 1L)
                .containsEntry("CANCELADA", 0L);
        assertThat(resultado.valorPorStatus().get("ABERTA")).isEqualByComparingTo("4000.00");
        assertThat(resultado.valorPorStatus().get("FECHADA")).isEqualByComparingTo("2000.00");
        assertThat(resultado.valorPorStatus().get("FATURADA")).isEqualByComparingTo("9000.00");

        // Soma dos valores por status deve bater com o valor total geral
        BigDecimal somaPorStatus = resultado.valorPorStatus().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(somaPorStatus).isEqualByComparingTo(resultado.valorTotalGeral());
    }

    @Test
    void faturamento_semContas_naoDeveGerarExcecaoEValorTotalDeveSerZero() {
        criarService();

        when(contaLookup.estatisticas()).thenReturn(
                new ContaEstatisticasDto(0, BigDecimal.ZERO, Map.of(), Map.of()));

        FaturamentoDashboardResponse resultado = service.faturamento();

        assertThat(resultado.totalContas()).isZero();
        assertThat(resultado.valorTotalGeral()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void faturamento_quandoFaturamentoIndisponivel_deveUsarFallbackSemQuebrar() {
        criarService();

        // Mesmo raciocínio de ocupacaoLeitos: aqui só garantimos que o DashboardService
        // não lança exceção quando o lookup devolve o fallback (zerado) do circuit breaker.
        when(contaLookup.estatisticas()).thenReturn(
                new ContaEstatisticasDto(0, BigDecimal.ZERO, Map.of(), Map.of()));

        FaturamentoDashboardResponse resultado = service.faturamento();

        assertThat(resultado.valorTotalGeral()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
