package com.soulmv.paciente.service;

import com.soulmv.paciente.dto.request.EnderecoDto;
import com.soulmv.paciente.dto.request.PacienteRequest;
import com.soulmv.paciente.dto.response.PacienteResponse;
import com.soulmv.paciente.entity.Endereco;
import com.soulmv.paciente.entity.Paciente;
import com.soulmv.paciente.enums.Sexo;
import com.soulmv.paciente.exception.BusinessException;
import com.soulmv.paciente.exception.ResourceNotFoundException;
import com.soulmv.paciente.mapper.PacienteMapper;
import com.soulmv.paciente.repository.PacienteRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    PacienteRepository pacienteRepository;

    @Mock
    PacienteMapper mapper;

    @InjectMocks
    PacienteService service;

    private PacienteRequest criarRequest(String cpf) {
        return new PacienteRequest(
                "Maria da Silva",
                cpf,
                "123456789012345",
                LocalDate.of(1990, 5, 10),
                Sexo.FEMININO,
                "11999998888",
                "maria@example.com",
                new EnderecoDto("Rua A", "100", "Apto 1", "Centro", "São Paulo", "SP", "01000-000"),
                7L,
                "CART-001"
        );
    }

    private Paciente pacienteExistente(Long id, String cpf) {
        return Paciente.builder()
                .nome("Maria da Silva")
                .cpf(cpf)
                .dataNascimento(LocalDate.of(1990, 5, 10))
                .sexo(Sexo.FEMININO)
                .telefone("11999998888")
                .email("maria@example.com")
                .endereco(new Endereco())
                .convenioId(7L)
                .numeroCarteirinha("CART-001")
                .build();
    }

    @Test
    void criar_deveSalvarPaciente_quandoCpfNaoExiste() {
        PacienteRequest request = criarRequest("11122233344");
        when(pacienteRepository.existsByCpf("11122233344")).thenReturn(false);
        when(mapper.toEntity(request.endereco())).thenReturn(new Endereco());
        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PacienteResponse resposta = new PacienteResponse(
                1L, "Maria da Silva", "11122233344", "123456789012345",
                request.dataNascimento(), Sexo.FEMININO, "11999998888", "maria@example.com",
                request.endereco(), 7L, "CART-001", null, null);
        when(mapper.toResponse(any(Paciente.class))).thenReturn(resposta);

        PacienteResponse resultado = service.criar(request);

        assertThat(resultado).isEqualTo(resposta);

        ArgumentCaptor<Paciente> captor = ArgumentCaptor.forClass(Paciente.class);
        verify(pacienteRepository).save(captor.capture());
        Paciente salvo = captor.getValue();
        assertThat(salvo.getNome()).isEqualTo("Maria da Silva");
        assertThat(salvo.getCpf()).isEqualTo("11122233344");
        assertThat(salvo.getSexo()).isEqualTo(Sexo.FEMININO);
        assertThat(salvo.getConvenioId()).isEqualTo(7L);
    }

    @Test
    void criar_deveUsarSexoNaoInformado_quandoSexoNaoInformadoNaRequisicao() {
        PacienteRequest request = new PacienteRequest(
                "João Souza", "22233344455", null,
                LocalDate.of(1985, 1, 1), null, null, null, null, null, null);
        when(pacienteRepository.existsByCpf("22233344455")).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(Paciente.class))).thenReturn(null);

        service.criar(request);

        ArgumentCaptor<Paciente> captor = ArgumentCaptor.forClass(Paciente.class);
        verify(pacienteRepository).save(captor.capture());
        assertThat(captor.getValue().getSexo()).isEqualTo(Sexo.NAO_INFORMADO);
    }

    @Test
    void criar_deveLancarBusinessException_quandoCpfDuplicado() {
        PacienteRequest request = criarRequest("99988877766");
        when(pacienteRepository.existsByCpf("99988877766")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe um paciente cadastrado com este CPF.");

        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void atualizar_deveAtualizarDados_quandoEncontrado() {
        Long id = 5L;
        Paciente existente = pacienteExistente(id, "11122233344");
        existente.setId(id);
        PacienteRequest request = criarRequest("11122233344");

        when(pacienteRepository.findById(id)).thenReturn(Optional.of(existente));
        when(mapper.toEntity(request.endereco())).thenReturn(new Endereco());
        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PacienteResponse resposta = new PacienteResponse(
                id, request.nome(), request.cpf(), request.cartaoSus(), request.dataNascimento(),
                request.sexo(), request.telefone(), request.email(), request.endereco(),
                request.convenioId(), request.numeroCarteirinha(), null, null);
        when(mapper.toResponse(any(Paciente.class))).thenReturn(resposta);

        PacienteResponse resultado = service.atualizar(id, request);

        assertThat(resultado).isEqualTo(resposta);
        verify(pacienteRepository, never()).existsByCpf(anyString());
        verify(pacienteRepository).save(existente);
        assertThat(existente.getNumeroCarteirinha()).isEqualTo("CART-001");
    }

    @Test
    void atualizar_deveValidarCpfDuplicado_quandoCpfAlteradoParaUmJaExistente() {
        Long id = 5L;
        Paciente existente = pacienteExistente(id, "11122233344");
        existente.setId(id);
        PacienteRequest request = criarRequest("55566677788");

        when(pacienteRepository.findById(id)).thenReturn(Optional.of(existente));
        when(pacienteRepository.existsByCpf("55566677788")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe um paciente cadastrado com este CPF.");

        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void atualizar_deveLancarResourceNotFoundException_quandoPacienteNaoExiste() {
        Long id = 999L;
        PacienteRequest request = criarRequest("11122233344");
        when(pacienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(id, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void buscarPorId_deveRetornarPaciente_quandoEncontrado() {
        Long id = 3L;
        Paciente paciente = pacienteExistente(id, "11122233344");
        paciente.setId(id);
        PacienteResponse resposta = new PacienteResponse(
                id, paciente.getNome(), paciente.getCpf(), paciente.getCartaoSus(),
                paciente.getDataNascimento(), paciente.getSexo(), paciente.getTelefone(),
                paciente.getEmail(), null, paciente.getConvenioId(), paciente.getNumeroCarteirinha(),
                null, null);
        when(pacienteRepository.findById(id)).thenReturn(Optional.of(paciente));
        when(mapper.toResponse(paciente)).thenReturn(resposta);

        PacienteResponse resultado = service.buscarPorId(id);

        assertThat(resultado).isEqualTo(resposta);
    }

    @Test
    void buscarPorId_deveLancarResourceNotFoundException_quandoNaoEncontrado() {
        Long id = 404L;
        when(pacienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(id));
    }

    @Test
    @SuppressWarnings("unchecked")
    void listar_deveChamarRepositoryComSpecificationEPageableCorretos() {
        Pageable pageable = PageRequest.of(0, 20);
        Paciente paciente = pacienteExistente(1L, "11122233344");
        Page<Paciente> pageEntidades = new PageImpl<>(List.of(paciente), pageable, 1);
        PacienteResponse resposta = new PacienteResponse(
                1L, paciente.getNome(), paciente.getCpf(), paciente.getCartaoSus(),
                paciente.getDataNascimento(), paciente.getSexo(), paciente.getTelefone(),
                paciente.getEmail(), null, paciente.getConvenioId(), paciente.getNumeroCarteirinha(),
                null, null);

        when(pacienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageEntidades);
        when(mapper.toResponse(paciente)).thenReturn(resposta);

        Page<PacienteResponse> resultado = service.listar("Maria", "11122233344", 7L, pageable);

        assertThat(resultado.getContent()).containsExactly(resposta);

        ArgumentCaptor<Specification<Paciente>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(pacienteRepository, times(1)).findAll(specCaptor.capture(), eq(pageable));
        assertThat(specCaptor.getValue()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void listar_devePermitirFiltrosNulos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pacienteRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<PacienteResponse> resultado = service.listar(null, null, null, pageable);

        assertThat(resultado.getContent()).isEmpty();
        verify(pacienteRepository).findAll(any(Specification.class), eq(pageable));
    }
}
