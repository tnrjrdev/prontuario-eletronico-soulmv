package com.soulmv.evolucao.controller;

import com.soulmv.evolucao.dto.request.EvolucaoRequest;
import com.soulmv.evolucao.dto.response.EvolucaoResponse;
import com.soulmv.evolucao.enums.TipoEvolucao;
import com.soulmv.evolucao.security.JwtService;
import com.soulmv.evolucao.service.EvolucaoService;
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
@RequestMapping("/api/atendimentos/{atendimentoId}/evolucoes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Evoluções", description = "Evolução clínica (médica e de enfermagem)")
public class EvolucaoController {

    private final EvolucaoService service;
    private final JwtService jwtService;

    public EvolucaoController(EvolucaoService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Registra uma evolução (tipo definido pelo perfil do autor)")
    public ResponseEntity<EvolucaoResponse> registrar(@PathVariable Long atendimentoId,
                                                       @Valid @RequestBody EvolucaoRequest request,
                                                       @RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        Claims claims = jwtService.extrairTodasClaims(token);
        TipoEvolucao tipo = resolverTipo(claims);
        EvolucaoResponse criada = service.registrar(atendimentoId, request,
                claims.get("uid", Long.class), claims.get("nome", String.class), tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista as evoluções do atendimento (mais recentes primeiro)")
    public ResponseEntity<List<EvolucaoResponse>> listar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.listar(atendimentoId));
    }

    @SuppressWarnings("unchecked")
    private TipoEvolucao resolverTipo(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        boolean medico = roles != null && roles.contains("MEDICO");
        return medico ? TipoEvolucao.MEDICA : TipoEvolucao.ENFERMAGEM;
    }
}
