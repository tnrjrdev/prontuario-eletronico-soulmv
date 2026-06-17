package com.soulmv.paciente.controller;

import com.soulmv.paciente.dto.request.PacienteRequest;
import com.soulmv.paciente.dto.response.PacienteResponse;
import com.soulmv.paciente.service.PacienteService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/pacientes")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('RECEPCAO','MEDICO','ENFERMEIRO')")
@Tag(name = "Pacientes", description = "Cadastro demográfico de pacientes (RECEPÇÃO/MÉDICO/ENFERMAGEM)")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    @Operation(summary = "Lista pacientes (paginado, filtros: nome, cpf, convenioId)")
    public ResponseEntity<Page<PacienteResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) Long convenioId,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(pacienteService.listar(nome, cpf, convenioId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um paciente por id")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um paciente")
    public ResponseEntity<PacienteResponse> criar(@Valid @RequestBody PacienteRequest request) {
        PacienteResponse criado = pacienteService.criar(request);
        return ResponseEntity.created(URI.create("/api/pacientes/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um paciente")
    public ResponseEntity<PacienteResponse> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody PacienteRequest request) {
        return ResponseEntity.ok(pacienteService.atualizar(id, request));
    }
}
