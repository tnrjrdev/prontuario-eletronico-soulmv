package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.request.AlocarLeitoRequest;
import com.soulmv.hospitalar.dto.request.AtendimentoRequest;
import com.soulmv.hospitalar.dto.request.AtendimentoStatusRequest;
import com.soulmv.hospitalar.dto.response.AtendimentoResponse;
import com.soulmv.hospitalar.enums.StatusAtendimento;
import com.soulmv.hospitalar.enums.TipoAtendimento;
import com.soulmv.hospitalar.service.AtendimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/atendimentos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Atendimentos", description = "Encontro do paciente: fila, status, internação e alta")
public class AtendimentoController {

    private final AtendimentoService service;

    public AtendimentoController(AtendimentoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPCAO','MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista/fila de atendimentos (filtros: status, tipo, setorId, pacienteId)")
    public ResponseEntity<Page<AtendimentoResponse>> listar(
            @RequestParam(required = false) StatusAtendimento status,
            @RequestParam(required = false) TipoAtendimento tipo,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) Long pacienteId,
            @PageableDefault(size = 20, sort = "dataEntrada") Pageable pageable) {
        return ResponseEntity.ok(service.listar(status, tipo, setorId, pacienteId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPCAO','MEDICO','ENFERMEIRO')")
    @Operation(summary = "Busca um atendimento por id")
    public ResponseEntity<AtendimentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPCAO','MEDICO','ENFERMEIRO')")
    @Operation(summary = "Abre um atendimento (entra na fila de triagem)")
    public ResponseEntity<AtendimentoResponse> abrir(@Valid @RequestBody AtendimentoRequest request) {
        AtendimentoResponse criado = service.abrir(request);
        return ResponseEntity.created(URI.create("/api/atendimentos/" + criado.id())).body(criado);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Altera o status do atendimento (exceto ALTA)")
    public ResponseEntity<AtendimentoResponse> atualizarStatus(@PathVariable Long id,
                                                               @Valid @RequestBody AtendimentoStatusRequest request,
                                                               Authentication authentication) {
        return ResponseEntity.ok(service.atualizarStatus(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/leito")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Aloca um leito ao atendimento (internação)")
    public ResponseEntity<AtendimentoResponse> alocarLeito(@PathVariable Long id,
                                                           @Valid @RequestBody AlocarLeitoRequest request) {
        return ResponseEntity.ok(service.alocarLeito(id, request));
    }

    @PostMapping("/{id}/alta")
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Dá alta ao paciente, encerrando o atendimento")
    public ResponseEntity<AtendimentoResponse> darAlta(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.darAlta(id, authentication.getName()));
    }
}
