package com.soulmv.catalogo.controller;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.SetorRequest;
import com.soulmv.catalogo.dto.response.SetorResponse;
import com.soulmv.catalogo.service.SetorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/setores")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Setores", description = "Cadastro de setores/unidades (escrita: ADMIN)")
public class SetorController {

    private final SetorService service;

    public SetorController(SetorService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista setores (paginado, filtro opcional por nome)")
    public ResponseEntity<Page<SetorResponse>> listar(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listar(q, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um setor por id")
    public ResponseEntity<SetorResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um setor")
    public ResponseEntity<SetorResponse> criar(@Valid @RequestBody SetorRequest request) {
        SetorResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/api/setores/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um setor")
    public ResponseEntity<SetorResponse> atualizar(@PathVariable Long id,
                                                   @Valid @RequestBody SetorRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativa/inativa um setor")
    public ResponseEntity<SetorResponse> atualizarStatus(@PathVariable Long id,
                                                         @Valid @RequestBody AtualizarStatusRequest request) {
        return ResponseEntity.ok(service.atualizarStatus(id, request));
    }
}
