package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.response.AtendimentosDashboardResponse;
import com.soulmv.hospitalar.dto.response.FaturamentoDashboardResponse;
import com.soulmv.hospitalar.dto.response.OcupacaoLeitosResponse;
import com.soulmv.hospitalar.enums.StatusAtendimento;
import com.soulmv.hospitalar.enums.StatusConta;
import com.soulmv.hospitalar.enums.StatusLeito;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.ContaHospitalarRepository;
import com.soulmv.hospitalar.repository.LeitoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Consultas agregadas para os painéis gerenciais.
 */
@Service
public class DashboardService {

    private final LeitoRepository leitoRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final ContaHospitalarRepository contaRepository;

    public DashboardService(LeitoRepository leitoRepository,
                            AtendimentoRepository atendimentoRepository,
                            ContaHospitalarRepository contaRepository) {
        this.leitoRepository = leitoRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.contaRepository = contaRepository;
    }

    @Transactional(readOnly = true)
    public OcupacaoLeitosResponse ocupacaoLeitos() {
        long total = leitoRepository.count();
        long ativos = leitoRepository.countByAtivoTrue();
        long ocupados = leitoRepository.countByStatus(StatusLeito.OCUPADO);
        long livres = leitoRepository.countByStatus(StatusLeito.LIVRE);

        Map<String, Long> porStatus = new LinkedHashMap<>();
        for (StatusLeito s : StatusLeito.values()) {
            porStatus.put(s.name(), leitoRepository.countByStatus(s));
        }

        double taxa = ativos > 0
                ? BigDecimal.valueOf(ocupados * 100.0 / ativos).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return new OcupacaoLeitosResponse(total, ativos, ocupados, livres, taxa, porStatus);
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

    @Transactional(readOnly = true)
    public FaturamentoDashboardResponse faturamento() {
        long total = contaRepository.count();
        BigDecimal valorTotal = contaRepository.somaValorTotal();

        Map<String, Long> contasPorStatus = new LinkedHashMap<>();
        Map<String, BigDecimal> valorPorStatus = new LinkedHashMap<>();
        for (StatusConta s : StatusConta.values()) {
            contasPorStatus.put(s.name(), contaRepository.countByStatus(s));
            valorPorStatus.put(s.name(), contaRepository.somaValorTotalPorStatus(s));
        }
        return new FaturamentoDashboardResponse(total, valorTotal, contasPorStatus, valorPorStatus);
    }
}
