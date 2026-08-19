package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.Cid10Request;
import com.soulmv.catalogo.dto.response.Cid10Response;
import com.soulmv.catalogo.entity.Cid10;
import com.soulmv.catalogo.exception.BusinessException;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.Cid10Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cid10Service é o único catálogo com exclusão física (excluir/delete),
 * ao invés de ativar/inativar — este teste cobre esse comportamento distinto.
 */
@ExtendWith(MockitoExtension.class)
class Cid10ServiceTest {

    @Mock
    Cid10Repository repository;

    @Mock
    ParametroMapper mapper;

    @InjectMocks
    Cid10Service service;

    private Cid10Request request() {
        return new Cid10Request("A00", "Cólera");
    }

    private Cid10 cid(Long id, String codigo) {
        Cid10 cid = Cid10.builder().codigo(codigo).descricao("Cólera").build();
        cid.setId(id);
        return cid;
    }

    @Test
    void criar_devePersistir_quandoCodigoNaoExiste() {
        when(repository.existsByCodigoIgnoreCase("A00")).thenReturn(false);
        when(repository.save(any(Cid10.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Cid10.class))).thenReturn(new Cid10Response(1L, "A00", "Cólera"));

        Cid10Response response = service.criar(request());

        assertThat(response.codigo()).isEqualTo("A00");
    }

    @Test
    void criar_deveFalhar_quandoCodigoJaExiste() {
        when(repository.existsByCodigoIgnoreCase("A00")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe uma CID com este código");

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_devePermitir_quandoCodigoNaoMudou() {
        Cid10 existente = cid(1L, "A00");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Cid10.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Cid10.class))).thenReturn(new Cid10Response(1L, "A00", "Cólera"));

        service.atualizar(1L, request());

        verify(repository, never()).existsByCodigoIgnoreCase(anyString());
    }

    @Test
    void atualizar_deveFalhar_quandoNovoCodigoJaExisteEmOutraCid() {
        Cid10 existente = cid(1L, "A01");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByCodigoIgnoreCase("A00")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, request()))
                .isInstanceOf(BusinessException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_deveFalhar_quandoCidNaoExiste() {
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
    void buscarPorId_deveRetornar_quandoExiste() {
        Cid10 existente = cid(1L, "A00");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(mapper.toResponse(existente)).thenReturn(new Cid10Response(1L, "A00", "Cólera"));

        Cid10Response response = service.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void listar_deveFiltrarPorCodigoOuDescricao_quandoQueryInformada() {
        Pageable pageable = PageRequest.of(0, 10);
        Cid10 existente = cid(1L, "A00");
        when(repository.findByCodigoContainingIgnoreCaseOrDescricaoContainingIgnoreCase("col", "col", pageable))
                .thenReturn(new PageImpl<>(List.of(existente)));
        when(mapper.toResponse(existente)).thenReturn(new Cid10Response(1L, "A00", "Cólera"));

        Page<Cid10Response> resultado = service.listar("col", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(repository, never()).findAll(pageable);
    }

    @Test
    void excluir_deveRemover_quandoExiste() {
        Cid10 existente = cid(1L, "A00");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.excluir(1L);

        verify(repository, times(1)).delete(existente);
    }

    @Test
    void excluir_deveFalhar_quandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
