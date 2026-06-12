package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.response.AtendimentosDashboardResponse;
import com.soulmv.hospitalar.dto.response.FaturamentoDashboardResponse;
import com.soulmv.hospitalar.dto.response.OcupacaoLeitosResponse;
import com.soulmv.hospitalar.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboards")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('FATURAMENTO','ADMIN')")
@Tag(name = "Dashboards", description = "Painéis gerenciais (FATURAMENTO/ADMIN)")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/ocupacao")
    @Operation(summary = "Ocupação de leitos e taxa de ocupação")
    public ResponseEntity<OcupacaoLeitosResponse> ocupacao() {
        return ResponseEntity.ok(service.ocupacaoLeitos());
    }

    @GetMapping("/atendimentos")
    @Operation(summary = "Total de atendimentos por status")
    public ResponseEntity<AtendimentosDashboardResponse> atendimentos() {
        return ResponseEntity.ok(service.atendimentos());
    }

    @GetMapping("/faturamento")
    @Operation(summary = "Contas e valores por status")
    public ResponseEntity<FaturamentoDashboardResponse> faturamento() {
        return ResponseEntity.ok(service.faturamento());
    }
}
