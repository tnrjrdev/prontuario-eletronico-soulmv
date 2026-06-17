package com.soulmv.iam.controller;

import com.soulmv.iam.dto.request.AtualizarRolesRequest;
import com.soulmv.iam.dto.request.AtualizarStatusRequest;
import com.soulmv.iam.dto.request.UsuarioCreateRequest;
import com.soulmv.iam.dto.request.UsuarioUpdateRequest;
import com.soulmv.iam.dto.response.ProfissionalResponse;
import com.soulmv.iam.dto.response.UsuarioResponse;
import com.soulmv.iam.service.UsuarioService;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "UsuÃ¡rios", description = "GestÃ£o de usuÃ¡rios e perfis (restrito ao ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Lista usuÃ¡rios (paginado)")
    public ResponseEntity<Page<UsuarioResponse>> listar(
            @PageableDefault(size = 20, sort = "nomeCompleto") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    @GetMapping("/profissionais")
    @PreAuthorize("hasAnyRole('RECEPCAO','MEDICO','ENFERMEIRO','ADMIN')")
    @Operation(summary = "Lista profissionais de saÃºde ativos (para seletores, ex.: agenda)")
    public ResponseEntity<List<ProfissionalResponse>> listarProfissionais() {
        return ResponseEntity.ok(usuarioService.listarProfissionais());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuÃ¡rio por id")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria um novo usuÃ¡rio")
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse criado = usuarioService.criar(request);
        return ResponseEntity.created(URI.create("/api/usuarios/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome e e-mail do usuÃ¡rio")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Ativa ou inativa um usuÃ¡rio")
    public ResponseEntity<UsuarioResponse> atualizarStatus(@PathVariable Long id,
                                                           @Valid @RequestBody AtualizarStatusRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarStatus(id, request));
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Substitui os perfis de um usuÃ¡rio")
    public ResponseEntity<UsuarioResponse> atualizarRoles(@PathVariable Long id,
                                                          @Valid @RequestBody AtualizarRolesRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarRoles(id, request));
    }
}
