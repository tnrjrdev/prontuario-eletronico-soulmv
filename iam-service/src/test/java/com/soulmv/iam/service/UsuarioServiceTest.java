package com.soulmv.iam.service;

import com.soulmv.iam.dto.request.AtualizarRolesRequest;
import com.soulmv.iam.dto.request.AtualizarStatusRequest;
import com.soulmv.iam.dto.request.UsuarioCreateRequest;
import com.soulmv.iam.dto.response.UsuarioResponse;
import com.soulmv.iam.entity.Usuario;
import com.soulmv.iam.enums.Role;
import com.soulmv.iam.exception.BusinessException;
import com.soulmv.iam.exception.ResourceNotFoundException;
import com.soulmv.iam.mapper.UsuarioMapper;
import com.soulmv.iam.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    UsuarioMapper usuarioMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UsuarioService service;

    private UsuarioCreateRequest criarRequestValido() {
        return new UsuarioCreateRequest(
                "Fulano de Tal",
                "fulano",
                "fulano@example.com",
                "senha123",
                Set.of(Role.ADMIN));
    }

    @Test
    void criar_devePersistirComSenhaHasheada_quandoLoginEEmailDisponiveis() {
        UsuarioCreateRequest request = criarRequestValido();

        when(usuarioRepository.existsByLogin(request.login())).thenReturn(false);
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("HASH_SEGURO");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(
                new UsuarioResponse(1L, request.nomeCompleto(), request.login(), request.email(),
                        true, request.roles(), null, null));

        UsuarioResponse response = service.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.login()).isEqualTo(request.login());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertThat(salvo.getSenhaHash()).isEqualTo("HASH_SEGURO");
        assertThat(salvo.getSenhaHash()).isNotEqualTo(request.senha());
        assertThat(salvo.isAtivo()).isTrue();
        assertThat(salvo.getRoles()).containsExactlyInAnyOrder(Role.ADMIN);

        verify(passwordEncoder).encode(request.senha());
    }

    @Test
    void criar_deveFalhar_quandoLoginJaExiste() {
        UsuarioCreateRequest request = criarRequestValido();
        when(usuarioRepository.existsByLogin(request.login())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("login");

        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void criar_deveFalhar_quandoEmailJaExiste() {
        UsuarioCreateRequest request = criarRequestValido();
        when(usuarioRepository.existsByLogin(request.login())).thenReturn(false);
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("e-mail");

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void atualizarStatus_deveInativarUsuario() {
        Usuario usuario = Usuario.builder()
                .nomeCompleto("Fulano")
                .login("fulano")
                .email("fulano@example.com")
                .senhaHash("hash")
                .ativo(true)
                .roles(new java.util.HashSet<>(Set.of(Role.ADMIN)))
                .build();
        usuario.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(
                new UsuarioResponse(1L, "Fulano", "fulano", "fulano@example.com", false, usuario.getRoles(), null, null));

        UsuarioResponse response = service.atualizarStatus(1L, new AtualizarStatusRequest(false));

        assertThat(response.ativo()).isFalse();
        assertThat(usuario.isAtivo()).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void atualizarStatus_deveFalhar_quandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarStatus(99L, new AtualizarStatusRequest(false)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void atualizarRoles_deveSubstituirConjuntoDeRoles() {
        Usuario usuario = Usuario.builder()
                .nomeCompleto("Fulano")
                .login("fulano")
                .email("fulano@example.com")
                .senhaHash("hash")
                .ativo(true)
                .roles(new java.util.HashSet<>(Set.of(Role.RECEPCAO)))
                .build();
        usuario.setId(2L);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(
                new UsuarioResponse(2L, "Fulano", "fulano", "fulano@example.com", true,
                        Set.of(Role.MEDICO, Role.ENFERMEIRO), null, null));

        UsuarioResponse response = service.atualizarRoles(2L, new AtualizarRolesRequest(Set.of(Role.MEDICO, Role.ENFERMEIRO)));

        assertThat(response.roles()).containsExactlyInAnyOrder(Role.MEDICO, Role.ENFERMEIRO);
        assertThat(usuario.getRoles()).containsExactlyInAnyOrder(Role.MEDICO, Role.ENFERMEIRO);
    }

    @Test
    void atualizarRoles_deveFalhar_quandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarRoles(42L, new AtualizarRolesRequest(Set.of(Role.ADMIN))))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarUsuario_quandoExiste() {
        Usuario usuario = Usuario.builder()
                .nomeCompleto("Fulano")
                .login("fulano")
                .email("fulano@example.com")
                .senhaHash("hash")
                .ativo(true)
                .roles(new java.util.HashSet<>(Set.of(Role.ADMIN)))
                .build();
        usuario.setId(5L);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(
                new UsuarioResponse(5L, "Fulano", "fulano", "fulano@example.com", true, usuario.getRoles(), null, null));

        UsuarioResponse response = service.buscarPorId(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.login()).isEqualTo("fulano");
    }

    @Test
    void buscarPorId_deveFalhar_quandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(123L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("123");

        verify(usuarioMapper, never()).toResponse(any());
    }
}
