package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.request.TriagemRequest;
import com.soulmv.hospitalar.dto.response.TriagemResponse;
import com.soulmv.hospitalar.service.TriagemService;
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
@RequestMapping("/api/atendimentos/{atendimentoId}/triagem")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Triagem", description = "Classificação de risco (Manchester)")
public class TriagemController {

    private final TriagemService service;

    public TriagemController(TriagemService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ENFERMEIRO')")
    @Operation(summary = "Registra a triagem do atendimento")
    public ResponseEntity<TriagemResponse> registrar(@PathVariable Long atendimentoId,
                                                     @Valid @RequestBody TriagemRequest request,
                                                     Authentication authentication) {
        TriagemResponse criada = service.registrar(atendimentoId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Consulta a triagem do atendimento")
    public ResponseEntity<TriagemResponse> buscar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.buscar(atendimentoId));
    }
}
