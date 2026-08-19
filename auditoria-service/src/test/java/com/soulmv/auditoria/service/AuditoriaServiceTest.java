package com.soulmv.auditoria.service;

import com.soulmv.auditoria.dto.response.AuditoriaResponse;
import com.soulmv.auditoria.entity.LogAuditoria;
import com.soulmv.auditoria.mapper.AuditoriaMapper;
import com.soulmv.auditoria.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    LogAuditoriaRepository repository;

    @Mock
    AuditoriaMapper mapper;

    @InjectMocks
    AuditoriaService service;

    @Test
    void registrar_deveSalvarLogComDadosCorretos() {
        LocalDateTime antes = LocalDateTime.now();

        service.registrar("usuario", "GET", "/api/x", 200, "127.0.0.1");

        LocalDateTime depois = LocalDateTime.now();

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(repository).save(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getUsuarioLogin()).isEqualTo("usuario");
        assertThat(log.getMetodo()).isEqualTo("GET");
        assertThat(log.getCaminho()).isEqualTo("/api/x");
        assertThat(log.getStatus()).isEqualTo(200);
        assertThat(log.getIp()).isEqualTo("127.0.0.1");
        assertThat(log.getDataHora()).isNotNull();
        assertThat(log.getDataHora()).isBetween(antes, depois);
    }

    @Test
    void registrar_devePropagarDadosDiferentesCorretamente() {
        service.registrar("outro.usuario", "POST", "/api/pacientes/123", 404, "10.0.0.5");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(repository).save(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getUsuarioLogin()).isEqualTo("outro.usuario");
        assertThat(log.getMetodo()).isEqualTo("POST");
        assertThat(log.getCaminho()).isEqualTo("/api/pacientes/123");
        assertThat(log.getStatus()).isEqualTo(404);
        assertThat(log.getIp()).isEqualTo("10.0.0.5");
    }

    @SuppressWarnings("unchecked")
    @Test
    void listar_devRetornarPaginaVaziaQuandoNaoHaResultados() {
        Pageable pageable = PageRequest.of(0, 30);
        Page<LogAuditoria> paginaVazia = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(paginaVazia);

        Page<AuditoriaResponse> resultado = service.listar("usuario", "/api", null, null, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(0);
        assertThat(resultado.getContent()).isEmpty();
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @SuppressWarnings("unchecked")
    @Test
    void listar_devRetornarPaginaMapeadaQuandoHaUmItem() {
        Pageable pageable = PageRequest.of(0, 30);
        LocalDateTime agora = LocalDateTime.now();

        LogAuditoria log = LogAuditoria.builder()
                .id(1L)
                .usuarioLogin("usuario")
                .metodo("GET")
                .caminho("/api/pacientes")
                .status(200)
                .ip("127.0.0.1")
                .dataHora(agora)
                .build();

        AuditoriaResponse response = new AuditoriaResponse(
                1L, "usuario", "GET", "/api/pacientes", 200, "127.0.0.1", agora);

        Page<LogAuditoria> paginaComItem = new PageImpl<>(List.of(log), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(paginaComItem);
        when(mapper.toResponse(log)).thenReturn(response);

        Page<AuditoriaResponse> resultado = service.listar("usuario", null, null, null, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent()).containsExactly(response);
        verify(repository).findAll(any(Specification.class), eq(pageable));
        verify(mapper).toResponse(log);
    }
}
