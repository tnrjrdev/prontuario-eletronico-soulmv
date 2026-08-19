package com.soulmv.sinaisvitais.controller;

import com.soulmv.sinaisvitais.dto.request.SinaisVitaisRequest;
import com.soulmv.sinaisvitais.dto.response.SinaisVitaisResponse;
import com.soulmv.sinaisvitais.security.JwtService;
import com.soulmv.sinaisvitais.service.SinaisVitaisService;
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
@RequestMapping("/api/atendimentos/{atendimentoId}/sinais-vitais")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sinais Vitais", description = "Registro de sinais vitais à beira-leito")
public class SinaisVitaisController {

    private final SinaisVitaisService service;
    private final JwtService jwtService;

    public SinaisVitaisController(SinaisVitaisService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENFERMEIRO','MEDICO')")
    @Operation(summary = "Registra sinais vitais do atendimento")
    public ResponseEntity<SinaisVitaisResponse> registrar(@PathVariable Long atendimentoId,
                                                          @Valid @RequestBody SinaisVitaisRequest request,
                                                          @RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        Claims claims = jwtService.extrairTodasClaims(token);
        SinaisVitaisResponse criado = service.registrar(atendimentoId, request,
                claims.get("uid", Long.class), claims.get("nome", String.class));
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista os sinais vitais do atendimento (mais recentes primeiro)")
    public ResponseEntity<List<SinaisVitaisResponse>> listar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.listar(atendimentoId));
    }
}
