package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.request.AdministracaoRequest;
import com.soulmv.hospitalar.dto.response.AdministracaoResponse;
import com.soulmv.hospitalar.service.AdministracaoService;
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
@RequestMapping("/api/itens-prescricao/{itemId}/administracoes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administração de medicação", description = "Checagem de medicação pela enfermagem")
public class AdministracaoController {

    private final AdministracaoService service;

    public AdministracaoController(AdministracaoService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ENFERMEIRO')")
    @Operation(summary = "Registra a checagem/administração de um item prescrito")
    public ResponseEntity<AdministracaoResponse> registrar(@PathVariable Long itemId,
                                                          @Valid @RequestBody AdministracaoRequest request,
                                                          Authentication authentication) {
        AdministracaoResponse criada = service.registrar(itemId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista as administrações registradas de um item")
    public ResponseEntity<List<AdministracaoResponse>> listar(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.listar(itemId));
    }
}
