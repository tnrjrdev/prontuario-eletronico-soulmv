package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.ConvenioRequest;
import com.soulmv.catalogo.dto.response.ConvenioResponse;
import com.soulmv.catalogo.entity.Convenio;
import com.soulmv.catalogo.enums.TipoConvenio;
import com.soulmv.catalogo.exception.BusinessException;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.ConvenioRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConvenioServiceTest {

    @Mock
    ConvenioRepository repository;

    @Mock
    ParametroMapper mapper;

    @InjectMocks
    ConvenioService service;

    private ConvenioRequest request() {
        return new ConvenioRequest("Unimed", "12345", TipoConvenio.PLANO_SAUDE);
    }

    private Convenio convenio(Long id, String nome) {
        Convenio convenio = Convenio.builder().nome(nome).tipo(TipoConvenio.PLANO_SAUDE).ativo(true).build();
        convenio.setId(id);
        return convenio;
    }

    @Test
    void criar_devePersistir_quandoNomeNaoExiste() {
        when(repository.existsByNomeIgnoreCase("Unimed")).thenReturn(false);
        when(repository.save(any(Convenio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Convenio.class))).thenReturn(
                new ConvenioResponse(1L, "Unimed", "12345", TipoConvenio.PLANO_SAUDE, true, null, null));

        ConvenioResponse response = service.criar(request());

        assertThat(response.nome()).isEqualTo("Unimed");
        assertThat(response.registroAns()).isEqualTo("12345");
    }

    @Test
    void criar_deveFalhar_quandoNomeJaExiste() {
        when(repository.existsByNomeIgnoreCase("Unimed")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe um convênio com este nome");

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_deveFalhar_quandoNovoNomeJaExisteEmOutroConvenio() {
        Convenio existente = convenio(1L, "Amil");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByNomeIgnoreCase("Unimed")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, request()))
                .isInstanceOf(BusinessException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_devePermitir_quandoNomeNaoMudou() {
        Convenio existente = convenio(1L, "Unimed");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Convenio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Convenio.class))).thenReturn(
                new ConvenioResponse(1L, "Unimed", "12345", TipoConvenio.PLANO_SAUDE, true, null, null));

        ConvenioResponse response = service.atualizar(1L, request());

        assertThat(response.nome()).isEqualTo("Unimed");
        verify(repository, never()).existsByNomeIgnoreCase(anyString());
    }

    @Test
    void atualizar_deveFalhar_quandoConvenioNaoExiste() {
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
        Convenio existente = convenio(1L, "Unimed");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(mapper.toResponse(existente)).thenReturn(
                new ConvenioResponse(1L, "Unimed", "12345", TipoConvenio.PLANO_SAUDE, true, null, null));

        ConvenioResponse response = service.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void listar_deveFiltrarPorNome_quandoQueryInformada() {
        Pageable pageable = PageRequest.of(0, 10);
        Convenio existente = convenio(1L, "Unimed");
        when(repository.findByNomeContainingIgnoreCase("Uni", pageable))
                .thenReturn(new PageImpl<>(List.of(existente)));
        when(mapper.toResponse(existente)).thenReturn(
                new ConvenioResponse(1L, "Unimed", "12345", TipoConvenio.PLANO_SAUDE, true, null, null));

        Page<ConvenioResponse> resultado = service.listar("Uni", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(repository, never()).findAll(pageable);
    }

    @Test
    void atualizarStatus_deveAlterarAtivo() {
        Convenio existente = convenio(1L, "Unimed");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Convenio.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Convenio.class))).thenReturn(
                new ConvenioResponse(1L, "Unimed", "12345", TipoConvenio.PLANO_SAUDE, false, null, null));

        service.atualizarStatus(1L, new AtualizarStatusRequest(false));

        assertThat(existente.isAtivo()).isFalse();
    }
}
