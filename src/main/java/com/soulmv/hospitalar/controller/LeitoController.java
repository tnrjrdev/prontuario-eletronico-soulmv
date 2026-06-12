package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.request.AtualizarStatusRequest;
import com.soulmv.hospitalar.dto.request.LeitoRequest;
import com.soulmv.hospitalar.dto.request.LeitoStatusRequest;
import com.soulmv.hospitalar.dto.response.LeitoResponse;
import com.soulmv.hospitalar.service.LeitoService;
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
@RequestMapping("/api/leitos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Leitos", description = "Cadastro de leitos e controle de status (escrita: ADMIN)")
public class LeitoController {

    private final LeitoService service;

    public LeitoController(LeitoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista leitos (paginado, filtro opcional por setor)")
    public ResponseEntity<Page<LeitoResponse>> listar(
            @RequestParam(required = false) Long setorId,
            @PageableDefault(size = 20, sort = "identificador") Pageable pageable) {
        return ResponseEntity.ok(service.listar(setorId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um leito por id")
    public ResponseEntity<LeitoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um leito")
    public ResponseEntity<LeitoResponse> criar(@Valid @RequestBody LeitoRequest request) {
        LeitoResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/api/leitos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza identificador/setor de um leito")
    public ResponseEntity<LeitoResponse> atualizar(@PathVariable Long id,
                                                   @Valid @RequestBody LeitoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','ENFERMEIRO')")
    @Operation(summary = "Altera o status operacional do leito (LIVRE, OCUPADO, ...)")
    public ResponseEntity<LeitoResponse> atualizarStatus(@PathVariable Long id,
                                                         @Valid @RequestBody LeitoStatusRequest request) {
        return ResponseEntity.ok(service.atualizarStatus(id, request));
    }

    @PatchMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativa/inativa um leito no cadastro")
    public ResponseEntity<LeitoResponse> atualizarAtivo(@PathVariable Long id,
                                                        @Valid @RequestBody AtualizarStatusRequest request) {
        return ResponseEntity.ok(service.atualizarAtivo(id, request));
    }
}
