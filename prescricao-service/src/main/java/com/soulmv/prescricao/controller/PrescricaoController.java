package com.soulmv.prescricao.controller;

import com.soulmv.prescricao.dto.request.PrescricaoRequest;
import com.soulmv.prescricao.dto.request.PrescricaoStatusRequest;
import com.soulmv.prescricao.dto.response.PrescricaoResponse;
import com.soulmv.prescricao.enums.StatusPrescricao;
import com.soulmv.prescricao.security.JwtService;
import com.soulmv.prescricao.service.PrescricaoService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Prescrições", description = "Prescrição médica e seus itens")
public class PrescricaoController {

    private final PrescricaoService service;
    private final JwtService jwtService;

    public PrescricaoController(PrescricaoService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping("/atendimentos/{atendimentoId}/prescricoes")
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Cria uma prescrição para o atendimento")
    public ResponseEntity<PrescricaoResponse> criar(@PathVariable Long atendimentoId,
                                                      @Valid @RequestBody PrescricaoRequest request,
                                                      @RequestHeader("Authorization") String authorization) {
        Claims claims = extrairClaims(authorization);
        PrescricaoResponse criada = service.criar(atendimentoId, request,
                claims.get("uid", Long.class), claims.get("nome", String.class));
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping("/atendimentos/{atendimentoId}/prescricoes")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista as prescrições do atendimento (mais recentes primeiro)")
    public ResponseEntity<List<PrescricaoResponse>> listar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.listar(atendimentoId));
    }

    @GetMapping("/prescricoes")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista todas as prescrições, com filtros opcionais")
    public ResponseEntity<Page<PrescricaoResponse>> listarTodas(
            @RequestParam(required = false) StatusPrescricao status,
            @RequestParam(required = false) Long pacienteId,
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listarTodas(status, pacienteId, pageable));
    }

    @PatchMapping("/prescricoes/{id}/status")
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Atualiza o status da prescrição")
    public ResponseEntity<PrescricaoResponse> atualizarStatus(@PathVariable Long id,
                                                                @Valid @RequestBody PrescricaoStatusRequest request) {
        return ResponseEntity.ok(service.atualizarStatus(id, request));
    }

    private Claims extrairClaims(String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        return jwtService.extrairTodasClaims(token);
    }
}
