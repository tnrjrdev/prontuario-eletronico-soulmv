package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.request.AtualizarStatusRequest;
import com.soulmv.hospitalar.dto.request.ProcedimentoTussRequest;
import com.soulmv.hospitalar.dto.response.ProcedimentoTussResponse;
import com.soulmv.hospitalar.service.ProcedimentoTussService;
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
@RequestMapping("/api/procedimentos-tuss")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Procedimentos TUSS", description = "Catálogo de procedimentos TUSS (escrita: ADMIN)")
public class ProcedimentoTussController {

    private final ProcedimentoTussService service;

    public ProcedimentoTussController(ProcedimentoTussService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista procedimentos (paginado, filtro opcional por código/descrição)")
    public ResponseEntity<Page<ProcedimentoTussResponse>> listar(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "descricao") Pageable pageable) {
        return ResponseEntity.ok(service.listar(q, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um procedimento por id")
    public ResponseEntity<ProcedimentoTussResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um procedimento TUSS")
    public ResponseEntity<ProcedimentoTussResponse> criar(@Valid @RequestBody ProcedimentoTussRequest request) {
        ProcedimentoTussResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/api/procedimentos-tuss/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um procedimento TUSS")
    public ResponseEntity<ProcedimentoTussResponse> atualizar(@PathVariable Long id,
                                                              @Valid @RequestBody ProcedimentoTussRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativa/inativa um procedimento TUSS")
    public ResponseEntity<ProcedimentoTussResponse> atualizarStatus(@PathVariable Long id,
                                                                    @Valid @RequestBody AtualizarStatusRequest request) {
        return ResponseEntity.ok(service.atualizarStatus(id, request));
    }
}
