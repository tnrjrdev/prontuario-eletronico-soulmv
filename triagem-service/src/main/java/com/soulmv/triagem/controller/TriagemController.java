package com.soulmv.triagem.controller;

import com.soulmv.triagem.dto.request.TriagemRequest;
import com.soulmv.triagem.dto.response.TriagemResponse;
import com.soulmv.triagem.security.JwtService;
import com.soulmv.triagem.service.TriagemService;
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
@RequestMapping("/api/atendimentos/{atendimentoId}/triagem")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Triagem", description = "Classificação de risco (Manchester)")
public class TriagemController {

    private final TriagemService service;
    private final JwtService jwtService;

    public TriagemController(TriagemService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ENFERMEIRO')")
    @Operation(summary = "Registra a triagem do atendimento")
    public ResponseEntity<TriagemResponse> registrar(@PathVariable Long atendimentoId,
                                                      @Valid @RequestBody TriagemRequest request,
                                                      @RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        Claims claims = jwtService.extrairTodasClaims(token);
        TriagemResponse criada = service.registrar(atendimentoId, request,
                claims.get("uid", Long.class), claims.get("nome", String.class));
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Consulta a triagem do atendimento")
    public ResponseEntity<TriagemResponse> buscar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.buscar(atendimentoId));
    }
}
