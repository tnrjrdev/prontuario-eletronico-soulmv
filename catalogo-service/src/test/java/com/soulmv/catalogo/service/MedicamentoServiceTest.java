package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.MedicamentoRequest;
import com.soulmv.catalogo.dto.response.MedicamentoResponse;
import com.soulmv.catalogo.entity.Medicamento;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.MedicamentoRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MedicamentoService não valida unicidade de nome (diferente dos demais
 * catálogos) — este teste cobre exatamente esse comportamento, além do CRUD básico.
 */
@ExtendWith(MockitoExtension.class)
class MedicamentoServiceTest {

    @Mock
    MedicamentoRepository repository;

    @Mock
    ParametroMapper mapper;

    @InjectMocks
    MedicamentoService service;

    private MedicamentoRequest request() {
        return new MedicamentoRequest("Dipirona", "Dipirona sódica", "500mg", false);
    }

    private Medicamento medicamento(Long id, String nome) {
        Medicamento medicamento = Medicamento.builder().nome(nome).ativo(true).controlado(false).build();
        medicamento.setId(id);
        return medicamento;
    }

    @Test
    void criar_devePersistir_semValidarDuplicidadeDeNome() {
        when(repository.save(any(Medicamento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Medicamento.class))).thenReturn(
                new MedicamentoResponse(1L, "Dipirona", "Dipirona sódica", "500mg", false, true, null, null));

        MedicamentoResponse response = service.criar(request());

        assertThat(response.nome()).isEqualTo("Dipirona");
        ArgumentCaptor<Medicamento> captor = ArgumentCaptor.forClass(Medicamento.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isAtivo()).isTrue();
        assertThat(captor.getValue().isControlado()).isFalse();
    }

    @Test
    void criar_devePersistirControlado_quandoInformado() {
        MedicamentoRequest controlado = new MedicamentoRequest("Morfina", "Sulfato de morfina", "10mg/mL", true);
        when(repository.save(any(Medicamento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Medicamento.class))).thenReturn(
                new MedicamentoResponse(2L, "Morfina", "Sulfato de morfina", "10mg/mL", true, true, null, null));

        service.criar(controlado);

        ArgumentCaptor<Medicamento> captor = ArgumentCaptor.forClass(Medicamento.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isControlado()).isTrue();
    }

    @Test
    void atualizar_deveAtualizarCampos() {
        Medicamento existente = medicamento(1L, "Dipirona");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Medicamento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Medicamento.class))).thenReturn(
                new MedicamentoResponse(1L, "Dipirona Sódica", "Dipirona", "1g", false, true, null, null));

        service.atualizar(1L, new MedicamentoRequest("Dipirona Sódica", "Dipirona", "1g", false));

        assertThat(existente.getNome()).isEqualTo("Dipirona Sódica");
        assertThat(existente.getConcentracao()).isEqualTo("1g");
    }

    @Test
    void atualizar_deveFalhar_quandoMedicamentoNaoExiste() {
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
        Medicamento existente = medicamento(1L, "Dipirona");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(mapper.toResponse(existente)).thenReturn(
                new MedicamentoResponse(1L, "Dipirona", null, null, false, true, null, null));

        MedicamentoResponse response = service.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void listar_deveFiltrarPorNome_quandoQueryInformada() {
        Pageable pageable = PageRequest.of(0, 10);
        Medicamento existente = medicamento(1L, "Dipirona");
        when(repository.findByNomeContainingIgnoreCase("Dipi", pageable))
                .thenReturn(new PageImpl<>(List.of(existente)));
        when(mapper.toResponse(existente)).thenReturn(
                new MedicamentoResponse(1L, "Dipirona", null, null, false, true, null, null));

        Page<MedicamentoResponse> resultado = service.listar("Dipi", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(repository, never()).findAll(pageable);
    }

    @Test
    void listar_deveListarTodos_quandoQueryVazia() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        service.listar("", pageable);

        verify(repository).findAll(pageable);
        verify(repository, never()).findByNomeContainingIgnoreCase(anyString(), any());
    }

    @Test
    void atualizarStatus_deveAlterarAtivo() {
        Medicamento existente = medicamento(1L, "Dipirona");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Medicamento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Medicamento.class))).thenReturn(
                new MedicamentoResponse(1L, "Dipirona", null, null, false, false, null, null));

        service.atualizarStatus(1L, new AtualizarStatusRequest(false));

        assertThat(existente.isAtivo()).isFalse();
    }
}
