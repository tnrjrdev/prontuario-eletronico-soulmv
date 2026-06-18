package com.soulmv.agendamento.controller;

import com.soulmv.agendamento.dto.request.AgendamentoRequest;
import com.soulmv.agendamento.dto.request.AgendamentoStatusRequest;
import com.soulmv.agendamento.dto.response.AgendamentoResponse;
import com.soulmv.agendamento.enums.StatusAgendamento;
import com.soulmv.agendamento.enums.TipoAgendamento;
import com.soulmv.agendamento.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/agendamentos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Agenda", description = "Marcação de consultas/exames, agenda do profissional e check-in")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPCAO','MEDICO','ENFERMEIRO','ADMIN')")
    @Operation(summary = "Lista a agenda (filtros: profissionalId, pacienteId, setorId, status, tipo, de, ate)")
    public ResponseEntity<Page<AgendamentoResponse>> listar(
            @RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) TipoAgendamento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate,
            @PageableDefault(size = 20, sort = "dataHora") Pageable pageable) {
        return ResponseEntity.ok(service.listar(profissionalId, pacienteId, setorId, status, tipo, de, ate, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPCAO','MEDICO','ENFERMEIRO','ADMIN')")
    @Operation(summary = "Busca um agendamento por id")
    public ResponseEntity<AgendamentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPCAO','ADMIN')")
    @Operation(summary = "Cria um agendamento")
    public ResponseEntity<AgendamentoResponse> criar(@Valid @RequestBody AgendamentoRequest request) {
        AgendamentoResponse criado = service.criar(request);
        return ResponseEntity.created(URI.create("/api/agendamentos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPCAO','ADMIN')")
    @Operation(summary = "Reagenda/atualiza um agendamento")
    public ResponseEntity<AgendamentoResponse> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody AgendamentoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECEPCAO','MEDICO','ENFERMEIRO','ADMIN')")
    @Operation(summary = "Altera o status (CONFIRMADO/CANCELADO/FALTOU)")
    public ResponseEntity<AgendamentoResponse> atualizarStatus(@PathVariable Long id,
                                                               @Valid @RequestBody AgendamentoStatusRequest request) {
        return ResponseEntity.ok(service.atualizarStatus(id, request));
    }

    @PostMapping("/{id}/checkin")
    @PreAuthorize("hasAnyRole('RECEPCAO','ADMIN')")
    @Operation(summary = "Check-in: gera o atendimento e marca o agendamento como realizado")
    public ResponseEntity<AgendamentoResponse> checkin(@PathVariable Long id) {
        return ResponseEntity.ok(service.checkin(id));
    }
}
