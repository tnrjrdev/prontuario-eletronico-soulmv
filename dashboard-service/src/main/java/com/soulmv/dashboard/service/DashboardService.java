package com.soulmv.dashboard.service;

import com.soulmv.dashboard.client.ContaEstatisticasDto;
import com.soulmv.dashboard.client.LeitoEstatisticasDto;
import com.soulmv.dashboard.dto.response.AtendimentosDashboardResponse;
import com.soulmv.dashboard.dto.response.FaturamentoDashboardResponse;
import com.soulmv.dashboard.dto.response.OcupacaoLeitosResponse;
import com.soulmv.dashboard.enums.StatusAtendimento;
import com.soulmv.dashboard.repository.AtendimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Consultas agregadas para os painéis gerenciais.
 *
 * <p>Ocupação (leitos) e faturamento (contas) buscam os dados via Feign nos serviços
 * donos (catalogo-service / faturamento-service) — antes liam direto do banco H2
 * compartilhado, um acoplamento por dados que só funcionava porque todo mundo ainda
 * apontava para o mesmo arquivo. Atendimentos continua lendo o banco compartilhado
 * diretamente: o domínio de Atendimento ainda não foi extraído do monólito (é a
 * "Frente 5" do plano de migração), então não existe hoje nenhum microsserviço dono
 * desses dados para chamar via API.</p>
 */
@Service
public class DashboardService {

    private final AtendimentoRepository atendimentoRepository;
    private final LeitoEstatisticasLookupService leitoLookup;
    private final ContaEstatisticasLookupService contaLookup;

    public DashboardService(AtendimentoRepository atendimentoRepository,
                            LeitoEstatisticasLookupService leitoLookup,
                            ContaEstatisticasLookupService contaLookup) {
        this.atendimentoRepository = atendimentoRepository;
        this.leitoLookup = leitoLookup;
        this.contaLookup = contaLookup;
    }

    public OcupacaoLeitosResponse ocupacaoLeitos() {
        LeitoEstatisticasDto dto = leitoLookup.estatisticas();

        double taxa = dto.ativos() > 0
                ? BigDecimal.valueOf(dto.ocupados() * 100.0 / dto.ativos()).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return new OcupacaoLeitosResponse(dto.total(), dto.ativos(), dto.ocupados(), dto.livres(), taxa, dto.porStatus());
    }

    @Transactional(readOnly = true)
    public AtendimentosDashboardResponse atendimentos() {
        long total = atendimentoRepository.count();
        Map<String, Long> porStatus = new LinkedHashMap<>();
        for (StatusAtendimento s : StatusAtendimento.values()) {
            porStatus.put(s.name(), atendimentoRepository.countByStatus(s));
        }
        return new AtendimentosDashboardResponse(total, porStatus);
    }

    public FaturamentoDashboardResponse faturamento() {
        ContaEstatisticasDto dto = contaLookup.estatisticas();
        return new FaturamentoDashboardResponse(dto.total(), dto.valorTotal(), dto.contasPorStatus(), dto.valorPorStatus());
    }
}
