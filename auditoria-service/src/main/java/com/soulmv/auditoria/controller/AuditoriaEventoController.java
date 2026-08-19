package com.soulmv.auditoria.controller;

import com.soulmv.auditoria.dto.request.EventoAuditoriaRequest;
import com.soulmv.auditoria.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint interno, chamado apenas pelo api-gateway a cada requisição da malha
 * (protegido por {@link com.soulmv.auditoria.security.InternalTokenFilter}, não por JWT
 * de usuário — não há usuário logado por trás dessa chamada). Não expor no frontend.
 */
@RestController
@RequestMapping("/api/auditoria/eventos")
@Tag(name = "Auditoria (interno)", description = "Recebe eventos de auditoria do api-gateway")
public class AuditoriaEventoController {

    private final AuditoriaService service;

    public AuditoriaEventoController(AuditoriaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registra um evento de auditoria (uso interno do api-gateway)")
    public ResponseEntity<Void> registrar(@Valid @RequestBody EventoAuditoriaRequest request) {
        service.registrar(request.usuarioLogin(), request.metodo(), request.caminho(),
                request.status(), request.ip());
        return ResponseEntity.accepted().build();
    }
}
