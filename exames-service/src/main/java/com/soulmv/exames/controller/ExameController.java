package com.soulmv.exames.controller;

import com.soulmv.exames.dto.request.ExameStatusRequest;
import com.soulmv.exames.dto.request.SolicitacaoExameRequest;
import com.soulmv.exames.dto.response.SolicitacaoExameResponse;
import com.soulmv.exames.enums.StatusExame;
import com.soulmv.exames.security.JwtService;
import com.soulmv.exames.service.ExameService;
import com.soulmv.exames.service.storage.ArquivoDownload;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Exames", description = "Solicitação de exames e liberação de laudos")
public class ExameController {

    private final ExameService service;
    private final JwtService jwtService;

    public ExameController(ExameService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping("/atendimentos/{atendimentoId}/exames")
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Solicita um exame")
    public ResponseEntity<SolicitacaoExameResponse> solicitar(@PathVariable Long atendimentoId,
                                                               @Valid @RequestBody SolicitacaoExameRequest request,
                                                               @RequestHeader("Authorization") String authorization) {
        Claims claims = extrairClaims(authorization);
        SolicitacaoExameResponse criada = service.solicitar(atendimentoId, request,
                claims.get("uid", Long.class), claims.get("nome", String.class));
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping("/atendimentos/{atendimentoId}/exames")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista os exames do atendimento")
    public ResponseEntity<List<SolicitacaoExameResponse>> listar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.listar(atendimentoId));
    }

    @GetMapping("/exames")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Listagem global de exames (filtros: status, pacienteId)")
    public ResponseEntity<Page<SolicitacaoExameResponse>> listarTodos(
            @RequestParam(required = false) StatusExame status,
            @RequestParam(required = false) Long pacienteId,
            @PageableDefault(size = 20, sort = "dataSolicitacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(status, pacienteId, pageable));
    }

    @PatchMapping("/exames/{id}/status")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Atualiza o status do exame (coleta, análise...)")
    public ResponseEntity<SolicitacaoExameResponse> atualizarStatus(@PathVariable Long id,
                                                                     @Valid @RequestBody ExameStatusRequest request) {
        return ResponseEntity.ok(service.atualizarStatus(id, request));
    }

    @PostMapping(value = "/exames/{id}/resultado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Libera o resultado/laudo do exame (texto e/ou PDF)")
    public ResponseEntity<SolicitacaoExameResponse> liberarResultado(
            @PathVariable Long id,
            @RequestParam(value = "resultadoTexto", required = false) String resultadoTexto,
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestHeader("Authorization") String authorization) {
        Claims claims = extrairClaims(authorization);
        return ResponseEntity.ok(service.liberarResultado(id, resultadoTexto, arquivo,
                claims.get("uid", Long.class), claims.get("nome", String.class)));
    }

    @GetMapping("/exames/{id}/laudo")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Baixa o arquivo do laudo do exame")
    public ResponseEntity<Resource> baixarLaudo(@PathVariable Long id) {
        ArquivoDownload download = service.baixarLaudo(id);
        String contentType = StringUtils.hasText(download.contentType())
                ? download.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.nomeOriginal() + "\"")
                .body(download.recurso());
    }

    private Claims extrairClaims(String authorization) {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        return jwtService.extrairTodasClaims(token);
    }
}
