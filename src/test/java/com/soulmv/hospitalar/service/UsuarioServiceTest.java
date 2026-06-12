package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.UsuarioCreateRequest;
import com.soulmv.hospitalar.dto.response.UsuarioResponse;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.enums.Role;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.UsuarioMapper;
import com.soulmv.hospitalar.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    UsuarioService usuarioService;

    private UsuarioCreateRequest request() {
        return new UsuarioCreateRequest("João Médico", "joao", "joao@x.com", "senha123", Set.of(Role.MEDICO));
    }

    @Test
    void criar_deveCriptografarSenha_eSalvar() {
        when(usuarioRepository.existsByLogin("joao")).thenReturn(false);
        when(usuarioRepository.existsByEmail("joao@x.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioMapper.toResponse(any(Usuario.class)))
                .thenReturn(new UsuarioResponse(1L, "João Médico", "joao", "joao@x.com", true,
                        Set.of(Role.MEDICO), null, null));

        UsuarioResponse resposta = usuarioService.criar(request());

        assertThat(resposta.login()).isEqualTo("joao");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        org.mockito.Mockito.verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getSenhaHash()).isEqualTo("HASH");
        assertThat(captor.getValue().isAtivo()).isTrue();
    }

    @Test
    void criar_deveFalhar_quandoLoginDuplicado() {
        when(usuarioRepository.existsByLogin("joao")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("login");
    }

    @Test
    void buscarPorId_deveFalhar_quandoNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
