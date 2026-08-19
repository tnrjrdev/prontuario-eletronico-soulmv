package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.SetorRequest;
import com.soulmv.catalogo.dto.response.SetorResponse;
import com.soulmv.catalogo.entity.Setor;
import com.soulmv.catalogo.enums.TipoSetor;
import com.soulmv.catalogo.exception.BusinessException;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.SetorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
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

@ExtendWith(MockitoExtension.class)
class SetorServiceTest {

    @Mock
    SetorRepository repository;

    @Mock
    ParametroMapper mapper;

    @InjectMocks
    SetorService service;

    private SetorRequest request() {
        return new SetorRequest("UTI Adulto", TipoSetor.UTI, "Unidade de terapia intensiva");
    }

    private Setor setor(Long id, String nome) {
        Setor setor = Setor.builder().nome(nome).tipo(TipoSetor.UTI).ativo(true).build();
        setor.setId(id);
        return setor;
    }

    @Test
    void criar_devePersistir_quandoNomeNaoExiste() {
        when(repository.existsByNomeIgnoreCase("UTI Adulto")).thenReturn(false);
        when(repository.save(any(Setor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Setor.class))).thenReturn(
                new SetorResponse(1L, "UTI Adulto", TipoSetor.UTI, "Unidade de terapia intensiva", true, null, null));

        SetorResponse response = service.criar(request());

        assertThat(response.nome()).isEqualTo("UTI Adulto");
        ArgumentCaptor<Setor> captor = ArgumentCaptor.forClass(Setor.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isAtivo()).isTrue();
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoSetor.UTI);
    }

    @Test
    void criar_deveFalhar_quandoNomeJaExiste() {
        when(repository.existsByNomeIgnoreCase("UTI Adulto")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe um setor com este nome");

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_devePermitir_quandoNomeNaoMudou() {
        Setor existente = setor(1L, "UTI Adulto");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Setor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Setor.class))).thenReturn(
                new SetorResponse(1L, "UTI Adulto", TipoSetor.UTI, null, true, null, null));

        SetorResponse response = service.atualizar(1L, request());

        assertThat(response.nome()).isEqualTo("UTI Adulto");
        verify(repository, never()).existsByNomeIgnoreCase(anyString());
    }

    @Test
    void atualizar_deveFalhar_quandoNovoNomeJaExisteEmOutroSetor() {
        Setor existente = setor(1L, "UTI Pediátrica");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByNomeIgnoreCase("UTI Adulto")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, request()))
                .isInstanceOf(BusinessException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_deveFalhar_quandoSetorNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, request()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void buscarPorId_deveRetornar_quandoExiste() {
        Setor existente = setor(1L, "UTI Adulto");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(mapper.toResponse(existente)).thenReturn(
                new SetorResponse(1L, "UTI Adulto", TipoSetor.UTI, null, true, null, null));

        SetorResponse response = service.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_deveFalhar_quandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listar_deveFiltrarPorNome_quandoQueryInformada() {
        Pageable pageable = PageRequest.of(0, 10);
        Setor existente = setor(1L, "UTI Adulto");
        when(repository.findByNomeContainingIgnoreCase("UTI", pageable))
                .thenReturn(new PageImpl<>(List.of(existente)));
        when(mapper.toResponse(existente)).thenReturn(
                new SetorResponse(1L, "UTI Adulto", TipoSetor.UTI, null, true, null, null));

        Page<SetorResponse> resultado = service.listar("UTI", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(repository, times(1)).findByNomeContainingIgnoreCase("UTI", pageable);
        verify(repository, never()).findAll(pageable);
    }

    @Test
    void listar_deveListarTodos_quandoQueryVazia() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        service.listar(null, pageable);

        verify(repository).findAll(pageable);
        verify(repository, never()).findByNomeContainingIgnoreCase(anyString(), any());
    }

    @Test
    void atualizarStatus_deveAlterarAtivo() {
        Setor existente = setor(1L, "UTI Adulto");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Setor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Setor.class))).thenReturn(
                new SetorResponse(1L, "UTI Adulto", TipoSetor.UTI, null, false, null, null));

        service.atualizarStatus(1L, new AtualizarStatusRequest(false));

        assertThat(existente.isAtivo()).isFalse();
    }
}
