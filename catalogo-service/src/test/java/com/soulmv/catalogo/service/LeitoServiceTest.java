package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.LeitoRequest;
import com.soulmv.catalogo.dto.request.LeitoStatusRequest;
import com.soulmv.catalogo.dto.response.LeitoEstatisticasResponse;
import com.soulmv.catalogo.dto.response.LeitoResponse;
import com.soulmv.catalogo.entity.Leito;
import com.soulmv.catalogo.entity.Setor;
import com.soulmv.catalogo.enums.StatusLeito;
import com.soulmv.catalogo.enums.TipoSetor;
import com.soulmv.catalogo.exception.BusinessException;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.LeitoRepository;
import com.soulmv.catalogo.repository.SetorRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LeitoService tem a regra de negócio mais elaborada dos catálogos: unicidade
 * do identificador é escopada por setor (não global) e a validação de
 * duplicidade só é reexecutada na atualização quando setor OU identificador mudam.
 */
@ExtendWith(MockitoExtension.class)
class LeitoServiceTest {

    @Mock
    LeitoRepository repository;

    @Mock
    SetorRepository setorRepository;

    @Mock
    ParametroMapper mapper;

    @InjectMocks
    LeitoService service;

    private Setor setor(Long id) {
        Setor setor = Setor.builder().nome("UTI").tipo(TipoSetor.UTI).ativo(true).build();
        setor.setId(id);
        return setor;
    }

    private Leito leito(Long id, String identificador, Setor setor) {
        Leito leito = Leito.builder().identificador(identificador).setor(setor)
                .status(StatusLeito.LIVRE).ativo(true).build();
        leito.setId(id);
        return leito;
    }

    @Test
    void criar_devePersistir_quandoIdentificadorUnicoNoSetor() {
        Setor setor = setor(1L);
        when(setorRepository.findById(1L)).thenReturn(Optional.of(setor));
        when(repository.existsBySetorIdAndIdentificadorIgnoreCase(1L, "UTI-01")).thenReturn(false);
        when(repository.save(any(Leito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Leito.class))).thenReturn(
                new LeitoResponse(1L, "UTI-01", 1L, "UTI", StatusLeito.LIVRE, true, null, null));

        LeitoResponse response = service.criar(new LeitoRequest("UTI-01", 1L));

        assertThat(response.identificador()).isEqualTo("UTI-01");
        ArgumentCaptor<Leito> captor = ArgumentCaptor.forClass(Leito.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusLeito.LIVRE);
        assertThat(captor.getValue().isAtivo()).isTrue();
    }

    @Test
    void criar_deveFalhar_quandoIdentificadorJaExisteNoMesmoSetor() {
        Setor setor = setor(1L);
        when(setorRepository.findById(1L)).thenReturn(Optional.of(setor));
        when(repository.existsBySetorIdAndIdentificadorIgnoreCase(1L, "UTI-01")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new LeitoRequest("UTI-01", 1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe um leito com este identificador neste setor");

        verify(repository, never()).save(any());
    }

    @Test
    void criar_deveFalhar_quandoSetorNaoExiste() {
        when(setorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(new LeitoRequest("UTI-01", 99L)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_devePermitir_quandoNemSetorNemIdentificadorMudaram() {
        Setor setor = setor(1L);
        Leito existente = leito(1L, "UTI-01", setor);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(setorRepository.findById(1L)).thenReturn(Optional.of(setor));
        when(repository.save(any(Leito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Leito.class))).thenReturn(
                new LeitoResponse(1L, "UTI-01", 1L, "UTI", StatusLeito.LIVRE, true, null, null));

        service.atualizar(1L, new LeitoRequest("UTI-01", 1L));

        verify(repository, never()).existsBySetorIdAndIdentificadorIgnoreCase(any(), any());
    }

    @Test
    void atualizar_deveValidarUnicidade_quandoIdentificadorMuda() {
        Setor setor = setor(1L);
        Leito existente = leito(1L, "UTI-01", setor);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(setorRepository.findById(1L)).thenReturn(Optional.of(setor));
        when(repository.existsBySetorIdAndIdentificadorIgnoreCase(1L, "UTI-02")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new LeitoRequest("UTI-02", 1L)))
                .isInstanceOf(BusinessException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void atualizar_deveValidarUnicidade_quandoSetorMuda() {
        Setor setorAtual = setor(1L);
        Setor novoSetor = setor(2L);
        Leito existente = leito(1L, "UTI-01", setorAtual);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(setorRepository.findById(2L)).thenReturn(Optional.of(novoSetor));
        when(repository.existsBySetorIdAndIdentificadorIgnoreCase(2L, "UTI-01")).thenReturn(false);
        when(repository.save(any(Leito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Leito.class))).thenReturn(
                new LeitoResponse(1L, "UTI-01", 2L, "Enfermaria", StatusLeito.LIVRE, true, null, null));

        service.atualizar(1L, new LeitoRequest("UTI-01", 2L));

        verify(repository).existsBySetorIdAndIdentificadorIgnoreCase(2L, "UTI-01");
        assertThat(existente.getSetor().getId()).isEqualTo(2L);
    }

    @Test
    void atualizarStatus_deveAlterarStatusOperacional() {
        Setor setor = setor(1L);
        Leito existente = leito(1L, "UTI-01", setor);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Leito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Leito.class))).thenReturn(
                new LeitoResponse(1L, "UTI-01", 1L, "UTI", StatusLeito.OCUPADO, true, null, null));

        service.atualizarStatus(1L, new LeitoStatusRequest(StatusLeito.OCUPADO));

        assertThat(existente.getStatus()).isEqualTo(StatusLeito.OCUPADO);
    }

    @Test
    void atualizarAtivo_deveAlterarFlagAtivo() {
        Setor setor = setor(1L);
        Leito existente = leito(1L, "UTI-01", setor);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Leito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Leito.class))).thenReturn(
                new LeitoResponse(1L, "UTI-01", 1L, "UTI", StatusLeito.LIVRE, false, null, null));

        service.atualizarAtivo(1L, new AtualizarStatusRequest(false));

        assertThat(existente.isAtivo()).isFalse();
    }

    @Test
    void buscarPorId_deveFalhar_quandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listar_deveFiltrarPorSetor_quandoSetorIdInformado() {
        Pageable pageable = PageRequest.of(0, 10);
        Setor setor = setor(1L);
        Leito existente = leito(1L, "UTI-01", setor);
        when(repository.findBySetorId(1L, pageable)).thenReturn(new PageImpl<>(List.of(existente)));
        when(mapper.toResponse(existente)).thenReturn(
                new LeitoResponse(1L, "UTI-01", 1L, "UTI", StatusLeito.LIVRE, true, null, null));

        Page<LeitoResponse> resultado = service.listar(1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(repository, never()).findAll(pageable);
    }

    @Test
    void listar_deveListarTodos_quandoSetorIdNaoInformado() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        service.listar(null, pageable);

        verify(repository).findAll(pageable);
        verify(repository, never()).findBySetorId(any(), any());
    }

    @Test
    void atualizarStatus_devePermitirOcupar_quandoLivreEAtivo() {
        Setor setor = setor(1L);
        Leito existente = leito(1L, "UTI-01", setor);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Leito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Leito.class))).thenReturn(
                new LeitoResponse(1L, "UTI-01", 1L, "UTI", StatusLeito.OCUPADO, true, null, null));

        service.atualizarStatus(1L, new LeitoStatusRequest(StatusLeito.OCUPADO));

        assertThat(existente.getStatus()).isEqualTo(StatusLeito.OCUPADO);
    }

    @Test
    void atualizarStatus_deveFalhar_quandoOcuparLeitoJaOcupado() {
        Setor setor = setor(1L);
        Leito existente = leito(1L, "UTI-01", setor);
        existente.setStatus(StatusLeito.OCUPADO);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizarStatus(1L, new LeitoStatusRequest(StatusLeito.OCUPADO)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");

        verify(repository, never()).save(any());
    }

    @Test
    void atualizarStatus_deveFalhar_quandoOcuparLeitoInativo() {
        Setor setor = setor(1L);
        Leito existente = leito(1L, "UTI-01", setor);
        existente.setAtivo(false);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizarStatus(1L, new LeitoStatusRequest(StatusLeito.OCUPADO)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inativo");

        verify(repository, never()).save(any());
    }

    @Test
    void atualizarStatus_devePermitirLiberar_semPrecondicao() {
        Setor setor = setor(1L);
        Leito existente = leito(1L, "UTI-01", setor);
        existente.setStatus(StatusLeito.OCUPADO);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Leito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Leito.class))).thenReturn(
                new LeitoResponse(1L, "UTI-01", 1L, "UTI", StatusLeito.HIGIENIZACAO, true, null, null));

        service.atualizarStatus(1L, new LeitoStatusRequest(StatusLeito.HIGIENIZACAO));

        assertThat(existente.getStatus()).isEqualTo(StatusLeito.HIGIENIZACAO);
    }

    @Test
    void estatisticas_deveContarPorStatusETotais() {
        when(repository.count()).thenReturn(10L);
        when(repository.countByAtivoTrue()).thenReturn(8L);
        when(repository.countByStatus(StatusLeito.OCUPADO)).thenReturn(5L);
        when(repository.countByStatus(StatusLeito.LIVRE)).thenReturn(3L);
        when(repository.countByStatus(StatusLeito.MANUTENCAO)).thenReturn(1L);
        when(repository.countByStatus(StatusLeito.HIGIENIZACAO)).thenReturn(1L);
        when(repository.countByStatus(StatusLeito.INTERDITADO)).thenReturn(0L);

        LeitoEstatisticasResponse resultado = service.estatisticas();

        assertThat(resultado.total()).isEqualTo(10L);
        assertThat(resultado.ativos()).isEqualTo(8L);
        assertThat(resultado.ocupados()).isEqualTo(5L);
        assertThat(resultado.livres()).isEqualTo(3L);
        assertThat(resultado.porStatus())
                .containsEntry("OCUPADO", 5L)
                .containsEntry("LIVRE", 3L)
                .containsEntry("MANUTENCAO", 1L);
    }
}
