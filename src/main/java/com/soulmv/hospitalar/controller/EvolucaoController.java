package com.soulmv.hospitalar.controller;

import com.soulmv.hospitalar.dto.request.EvolucaoRequest;
import com.soulmv.hospitalar.dto.response.EvolucaoResponse;
import com.soulmv.hospitalar.enums.TipoEvolucao;
import com.soulmv.hospitalar.service.EvolucaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/atendimentos/{atendimentoId}/evolucoes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Evoluções", description = "Evolução clínica (médica e de enfermagem)")
public class EvolucaoController {

    private final EvolucaoService service;

    public EvolucaoController(EvolucaoService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Registra uma evolução (tipo definido pelo perfil do autor)")
    public ResponseEntity<EvolucaoResponse> registrar(@PathVariable Long atendimentoId,
                                                      @Valid @RequestBody EvolucaoRequest request,
                                                      Authentication authentication) {
        TipoEvolucao tipo = resolverTipo(authentication);
        EvolucaoResponse criada = service.registrar(atendimentoId, request, authentication.getName(), tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
    @Operation(summary = "Lista as evoluções do atendimento (mais recentes primeiro)")
    public ResponseEntity<List<EvolucaoResponse>> listar(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.listar(atendimentoId));
    }

    private TipoEvolucao resolverTipo(Authentication authentication) {
        boolean medico = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MEDICO"::equals);
        return medico ? TipoEvolucao.MEDICA : TipoEvolucao.ENFERMAGEM;
    }
}
