package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.ProcedimentoTussRequest;
import com.soulmv.catalogo.dto.response.ProcedimentoTussResponse;
import com.soulmv.catalogo.entity.ProcedimentoTuss;
import com.soulmv.catalogo.exception.BusinessException;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.ProcedimentoTussRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcedimentoTussServiceTest {

    @Mock
    ProcedimentoTussRepository repository;

    @Mock
    ParametroMapper mapper;

    @InjectMocks
    ProcedimentoTussService service;

    private ProcedimentoTussRequest request() {
        return new ProcedimentoTussRequest("10101012", "Consulta médica", BigDecimal.valueOf(150));
    }

    private ProcedimentoTuss procedimento(Long id, String codigo) {
        ProcedimentoTuss procedimento = ProcedimentoTuss.builder().codigoTuss(codigo).descricao("Consulta médica").ativo(true).build();
        procedimento.setId(id);
        return procedimento;
    }

    @Test
    void criar_devePersistir_quandoCodigoNaoExiste() {
        when(repository.existsByCodigoTuss("10101012")).thenReturn(false);
        when(repository.save(any(ProcedimentoTuss.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(ProcedimentoTuss.class))).thenReturn(
                new ProcedimentoTussResponse(1L, "10101012", "Consulta médica", BigDecimal.valueOf(150), true, null, null));

        ProcedimentoTussResponse response = service.criar(request());

        assertThat(response.codigoTuss()).isEqualTo("10101012");
    }

    @Test
    void criar_deveFalhar_quandoCodigoJaExiste() {
        when(repository.existsByCodigoTuss("10101012")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("código TUSS");

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_devePermitir_quandoCodigoNaoMudou() {
        ProcedimentoTuss existente = procedimento(1L, "10101012");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(ProcedimentoTuss.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(ProcedimentoTuss.class))).thenReturn(
                new ProcedimentoTussResponse(1L, "10101012", "Consulta médica", BigDecimal.valueOf(150), true, null, null));

        service.atualizar(1L, request());

        verify(repository, never()).existsByCodigoTuss(anyString());
    }

    @Test
    void atualizar_deveFalhar_quandoNovoCodigoJaExisteEmOutroProcedimento() {
        ProcedimentoTuss existente = procedimento(1L, "20202020");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByCodigoTuss("10101012")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, request()))
                .isInstanceOf(BusinessException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_deveFalhar_quandoProcedimentoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, request()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void buscarPorId_deveFalhar_quandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listar_deveFiltrarPorCodigoOuDescricao_quandoQueryInformada() {
        Pageable pageable = PageRequest.of(0, 10);
        ProcedimentoTuss existente = procedimento(1L, "10101012");
        when(repository.findByCodigoTussContainingIgnoreCaseOrDescricaoContainingIgnoreCase("consulta", "consulta", pageable))
                .thenReturn(new PageImpl<>(List.of(existente)));
        when(mapper.toResponse(existente)).thenReturn(
                new ProcedimentoTussResponse(1L, "10101012", "Consulta médica", BigDecimal.valueOf(150), true, null, null));

        Page<ProcedimentoTussResponse> resultado = service.listar("consulta", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(repository, never()).findAll(pageable);
    }

    @Test
    void atualizarStatus_deveAlterarAtivo() {
        ProcedimentoTuss existente = procedimento(1L, "10101012");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(ProcedimentoTuss.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(ProcedimentoTuss.class))).thenReturn(
                new ProcedimentoTussResponse(1L, "10101012", "Consulta médica", BigDecimal.valueOf(150), false, null, null));

        service.atualizarStatus(1L, new AtualizarStatusRequest(false));

        assertThat(existente.isAtivo()).isFalse();
    }
}
