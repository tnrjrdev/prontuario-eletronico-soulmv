package com.soulmv.faturamento.controller;

import com.soulmv.faturamento.dto.request.ContaRequest;
import com.soulmv.faturamento.dto.request.ItemContaRequest;
import com.soulmv.faturamento.dto.response.ContaResponse;
import com.soulmv.faturamento.dto.response.GuiaTissResponse;
import com.soulmv.faturamento.enums.StatusConta;
import com.soulmv.faturamento.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('FATURAMENTO')")
@Tag(name = "Faturamento", description = "Contas hospitalares, itens TUSS e guias TISS (perfil FATURAMENTO)")
public class ContaController {

    private final ContaService service;

    public ContaController(ContaService service) {
        this.service = service;
    }

    @PostMapping("/contas")
    @Operation(summary = "Abre uma conta hospitalar para um atendimento")
    public ResponseEntity<ContaResponse> abrir(@Valid @RequestBody ContaRequest request) {
        ContaResponse criada = service.abrir(request);
        return ResponseEntity.created(URI.create("/api/contas/" + criada.id())).body(criada);
    }

    @GetMapping("/contas")
    @Operation(summary = "Lista contas (filtro opcional por status)")
    public ResponseEntity<Page<ContaResponse>> listar(
            @RequestParam(required = false) StatusConta status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listar(status, pageable));
    }

    @GetMapping("/contas/{id}")
    @Operation(summary = "Busca uma conta por id")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping("/contas/{id}/itens")
    @Operation(summary = "Adiciona um item (procedimento TUSS) à conta")
    public ResponseEntity<ContaResponse> adicionarItem(@PathVariable Long id,
                                                       @Valid @RequestBody ItemContaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionarItem(id, request));
    }

    @PostMapping("/contas/{id}/fechar")
    @Operation(summary = "Fecha a conta (impede novos itens)")
    public ResponseEntity<ContaResponse> fechar(@PathVariable Long id) {
        return ResponseEntity.ok(service.fechar(id));
    }

    @PostMapping("/contas/{id}/guias-tiss")
    @Operation(summary = "Gera a guia TISS (XML) da conta e marca como FATURADA")
    public ResponseEntity<GuiaTissResponse> gerarGuia(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.gerarGuiaTiss(id));
    }

    @GetMapping("/contas/{id}/guias-tiss")
    @Operation(summary = "Lista as guias TISS geradas para a conta")
    public ResponseEntity<List<GuiaTissResponse>> listarGuias(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarGuias(id));
    }

    @GetMapping(value = "/guias-tiss/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Retorna o XML TISS gerado")
    public ResponseEntity<String> obterXml(@PathVariable Long id) {
        return ResponseEntity.ok(service.obterXmlGuia(id));
    }
}
