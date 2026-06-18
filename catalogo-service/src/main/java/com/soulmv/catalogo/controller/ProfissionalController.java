package com.soulmv.catalogo.controller;

import com.soulmv.catalogo.client.ProfissionalDto;
import com.soulmv.catalogo.service.ProfissionalLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint de conveniência que agrega dados de outro serviço (iam) via Feign,
 * demonstrando comunicação entre serviços com resiliência (circuit breaker).
 */
@RestController
@RequestMapping("/api/catalogos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Catálogos (agregado)", description = "Dados agregados de outros serviços")
public class ProfissionalController {

    private final ProfissionalLookupService service;

    public ProfissionalController(ProfissionalLookupService service) {
        this.service = service;
    }

    @GetMapping("/profissionais")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCAO','MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista profissionais (via iam-service, resiliente)")
    public ResponseEntity<List<ProfissionalDto>> profissionais() {
        return ResponseEntity.ok(service.listarProfissionais());
    }
}
