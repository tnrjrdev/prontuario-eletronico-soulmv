package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.request.AnamneseRequest;
import com.soulmv.hospitalar.dto.response.AnamneseResponse;
import com.soulmv.hospitalar.service.AnamneseService;
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

@RestController
@RequestMapping("/api/atendimentos/{atendimentoId}/anamnese")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Anamnese", description = "Anamnese do atendimento (registro: MÉDICO)")
public class AnamneseController {

    private final AnamneseService service;

    public AnamneseController(AnamneseService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Registra a anamnese do atendimento")
    public ResponseEntity<AnamneseResponse> registrar(@PathVariable Long atendimentoId,
                                                      @Valid @RequestBody AnamneseRequest request,
                                                      Authentication authentication) {
        AnamneseResponse criada = service.registrar(atendimentoId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Consulta a anamnese do atendimento")
    public ResponseEntity<AnamneseResponse> buscar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.buscar(atendimentoId));
    }
}
