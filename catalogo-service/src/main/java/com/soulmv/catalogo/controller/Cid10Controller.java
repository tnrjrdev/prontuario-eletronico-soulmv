package com.soulmv.catalogo.controller;

import com.soulmv.catalogo.dto.request.Cid10Request;
import com.soulmv.catalogo.dto.response.Cid10Response;
import com.soulmv.catalogo.service.Cid10Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/cid10")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "CID-10", description = "Catálogo CID-10 (escrita: ADMIN)")
public class Cid10Controller {

    private final Cid10Service service;

    public Cid10Controller(Cid10Service service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista CIDs (paginado, filtro opcional por código/descrição)")
    public ResponseEntity<Page<Cid10Response>> listar(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "codigo") Pageable pageable) {
        return ResponseEntity.ok(service.listar(q, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma CID por id")
    public ResponseEntity<Cid10Response> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria uma CID")
    public ResponseEntity<Cid10Response> criar(@Valid @RequestBody Cid10Request request) {
        Cid10Response criado = service.criar(request);
        return ResponseEntity.created(URI.create("/api/cid10/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza uma CID")
    public ResponseEntity<Cid10Response> atualizar(@PathVariable Long id,
                                                   @Valid @RequestBody Cid10Request request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui uma CID")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
