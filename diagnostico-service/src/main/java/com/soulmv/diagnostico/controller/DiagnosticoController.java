package com.soulmv.diagnostico.controller;

import com.soulmv.diagnostico.dto.request.DiagnosticoRequest;
import com.soulmv.diagnostico.dto.response.DiagnosticoResponse;
import com.soulmv.diagnostico.security.JwtService;
import com.soulmv.diagnostico.service.DiagnosticoService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/atendimentos/{atendimentoId}/diagnosticos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Diagnósticos", description = "Diagnósticos CID-10 do atendimento (registro: MÉDICO)")
public class DiagnosticoController {

    private final DiagnosticoService service;
    private final JwtService jwtService;

    public DiagnosticoController(DiagnosticoService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Adiciona um diagnóstico ao atendimento")
    public ResponseEntity<DiagnosticoResponse> adicionar(@PathVariable Long atendimentoId,
                                                         @Valid @RequestBody DiagnosticoRequest request,
                                                         @RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        Claims claims = jwtService.extrairTodasClaims(token);
        DiagnosticoResponse criado = service.adicionar(atendimentoId, request,
                claims.get("uid", Long.class), claims.get("nome", String.class));
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista os diagnósticos do atendimento")
    public ResponseEntity<List<DiagnosticoResponse>> listar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.listar(atendimentoId));
    }
}
