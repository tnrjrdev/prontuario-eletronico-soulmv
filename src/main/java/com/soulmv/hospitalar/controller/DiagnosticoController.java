package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.request.DiagnosticoRequest;
import com.soulmv.hospitalar.dto.response.DiagnosticoResponse;
import com.soulmv.hospitalar.service.DiagnosticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/atendimentos/{atendimentoId}/diagnosticos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Diagnósticos", description = "Diagnósticos CID-10 do atendimento (registro: MÉDICO)")
public class DiagnosticoController {

    private final DiagnosticoService service;

    public DiagnosticoController(DiagnosticoService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Adiciona um diagnóstico ao atendimento")
    public ResponseEntity<DiagnosticoResponse> adicionar(@PathVariable Long atendimentoId,
                                                         @Valid @RequestBody DiagnosticoRequest request,
                                                         Authentication authentication) {
        DiagnosticoResponse criado = service.adicionar(atendimentoId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista os diagnósticos do atendimento")
    public ResponseEntity<List<DiagnosticoResponse>> listar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.listar(atendimentoId));
    }
}
