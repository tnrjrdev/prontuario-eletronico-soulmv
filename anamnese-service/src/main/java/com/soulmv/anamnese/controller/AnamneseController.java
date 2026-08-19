package com.soulmv.anamnese.controller;

import com.soulmv.anamnese.dto.request.AnamneseRequest;
import com.soulmv.anamnese.dto.response.AnamneseResponse;
import com.soulmv.anamnese.security.JwtService;
import com.soulmv.anamnese.service.AnamneseService;
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

@RestController
@RequestMapping("/api/atendimentos/{atendimentoId}/anamnese")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Anamnese", description = "Anamnese do atendimento (registro: MÉDICO)")
public class AnamneseController {

    private final AnamneseService service;
    private final JwtService jwtService;

    public AnamneseController(AnamneseService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Registra a anamnese do atendimento")
    public ResponseEntity<AnamneseResponse> registrar(@PathVariable Long atendimentoId,
                                                       @Valid @RequestBody AnamneseRequest request,
                                                       @RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        Claims claims = jwtService.extrairTodasClaims(token);
        AnamneseResponse criada = service.registrar(atendimentoId, request,
                claims.get("uid", Long.class), claims.get("nome", String.class));
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Consulta a anamnese do atendimento")
    public ResponseEntity<AnamneseResponse> buscar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.buscar(atendimentoId));
    }
}
