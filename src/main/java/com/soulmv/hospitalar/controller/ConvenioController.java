package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.request.AtualizarStatusRequest;
import com.soulmv.hospitalar.dto.request.ConvenioRequest;
import com.soulmv.hospitalar.dto.response.ConvenioResponse;
import com.soulmv.hospitalar.service.ConvenioService;
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
@RequestMapping("/api/convenios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Convênios", description = "Cadastro de convênios/planos (escrita: ADMIN)")
public class ConvenioController {

    private final ConvenioService service;

    public ConvenioController(ConvenioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista convênios (paginado, filtro opcional por nome)")
    public ResponseEntity<Page<ConvenioResponse>> listar(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listar(q, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um convênio por id")
    public ResponseEntity<ConvenioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um convênio")
    public ResponseEntity<ConvenioResponse> criar(@Valid @RequestBody ConvenioRequest request) {
        ConvenioResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/api/convenios/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um convênio")
    public ResponseEntity<ConvenioResponse> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ConvenioRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativa/inativa um convênio")
    public ResponseEntity<ConvenioResponse> atualizarStatus(@PathVariable Long id,
                                                            @Valid @RequestBody AtualizarStatusRequest request) {
        return ResponseEntity.ok(service.atualizarStatus(id, request));
    }
}
