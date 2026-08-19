package com.soulmv.prescricao.controller;

import com.soulmv.prescricao.dto.request.AdministracaoRequest;
import com.soulmv.prescricao.dto.response.AdministracaoResponse;
import com.soulmv.prescricao.security.JwtService;
import com.soulmv.prescricao.service.AdministracaoService;
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
@RequestMapping("/api/itens-prescricao/{itemId}/administracoes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administração de Medicamentos", description = "Checagem de administração de itens de prescrição")
public class AdministracaoController {

    private final AdministracaoService service;
    private final JwtService jwtService;

    public AdministracaoController(AdministracaoService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ENFERMEIRO')")
    @Operation(summary = "Registra a administração (checagem) de um item de prescrição")
    public ResponseEntity<AdministracaoResponse> registrar(@PathVariable Long itemId,
                                                             @Valid @RequestBody AdministracaoRequest request,
                                                             @RequestHeader("Authorization") String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        Claims claims = jwtService.extrairTodasClaims(token);
        AdministracaoResponse criada = service.registrar(itemId, request,
                claims.get("uid", Long.class), claims.get("nome", String.class));
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista as administrações registradas para o item")
    public ResponseEntity<List<AdministracaoResponse>> listar(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.listar(itemId));
    }
}
