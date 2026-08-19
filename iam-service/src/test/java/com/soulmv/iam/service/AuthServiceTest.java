package com.soulmv.iam.service;

import com.soulmv.iam.dto.request.LoginRequest;
import com.soulmv.iam.dto.response.TokenResponse;
import com.soulmv.iam.dto.response.UsuarioResponse;
import com.soulmv.iam.entity.Usuario;
import com.soulmv.iam.enums.Role;
import com.soulmv.iam.exception.BusinessException;
import com.soulmv.iam.mapper.UsuarioMapper;
import com.soulmv.iam.repository.UsuarioRepository;
import com.soulmv.iam.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    JwtService jwtService;

    @Mock
    UsuarioMapper usuarioMapper;

    @InjectMocks
    AuthService service;

    private Usuario usuarioValido() {
        Usuario usuario = Usuario.builder()
                .nomeCompleto("Fulano de Tal")
                .login("fulano")
                .email("fulano@example.com")
                .senhaHash("hash")
                .ativo(true)
                .roles(new HashSet<>(Set.of(Role.MEDICO)))
                .build();
        usuario.setId(1L);
        return usuario;
    }

    @Test
    void login_deveGerarTokens_quandoCredenciaisValidas() {
        LoginRequest request = new LoginRequest("fulano", "senha123");
        Usuario usuario = usuarioValido();

        when(usuarioRepository.findByLogin("fulano")).thenReturn(Optional.of(usuario));
        when(jwtService.gerarAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.gerarRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiracaoSegundos()).thenReturn(900L);
        when(usuarioMapper.toResponse(usuario)).thenReturn(
                new UsuarioResponse(1L, usuario.getNomeCompleto(), usuario.getLogin(), usuario.getEmail(),
                        true, usuario.getRoles(), null, null));

        TokenResponse response = service.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
        assertThat(response.usuario().login()).isEqualTo("fulano");

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("fulano", "senha123"));
    }

    @Test
    void login_deveFalhar_quandoSenhaIncorreta() {
        LoginRequest request = new LoginRequest("fulano", "senhaErrada");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Nota: não fixamos o texto exato da mensagem porque o arquivo-fonte
        // AuthService.java está com um problema de codificação pré-existente
        // (acentos gravados como sequências mal-formadas, ex.: "invÃ¡lidos" em vez
        // de "inválidos") — ver achado reportado separadamente. Validamos o
        // status HTTP e o comportamento, que são o que importa para este teste.
        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED);

        // não deve nem consultar o repositório após falha de autenticação
        verify(usuarioRepository, never()).findByLogin(any());
        verify(jwtService, never()).gerarAccessToken(any());
    }

    @Test
    void login_deveFalhar_comMesmoStatus_quandoUsuarioNaoExisteNoRepositorioAposAutenticar() {
        // Cenário defensivo: autenticação passou (ex.: cache/inconsistência), mas o
        // usuário não é encontrado no repositório ao gerar os tokens.
        LoginRequest request = new LoginRequest("inexistente", "qualquer");

        when(usuarioRepository.findByLogin("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getStatus())
                .isEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_deveUsarMesmaMensagemDeErro_paraSenhaIncorretaEUsuarioInexistente() {
        // Prática de segurança: a mensagem de erro deve ser idêntica nos dois
        // cenários (mesmo texto, byte a byte), para não permitir enumeração de
        // usuários válidos por quem observa a resposta da API. Comparamos as
        // duas mensagens entre si (sem fixar o texto literal) por causa do
        // problema de codificação do arquivo-fonte mencionado acima.
        LoginRequest senhaErrada = new LoginRequest("fulano", "senhaErrada");
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("fulano", "senhaErrada")))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        String mensagemSenhaErrada = catchMessage(() -> service.login(senhaErrada));

        LoginRequest usuarioInexistente = new LoginRequest("inexistente", "qualquer");
        when(usuarioRepository.findByLogin("inexistente")).thenReturn(Optional.empty());
        String mensagemUsuarioInexistente = catchMessage(() -> service.login(usuarioInexistente));

        assertThat(mensagemSenhaErrada)
                .isNotNull()
                .isEqualTo(mensagemUsuarioInexistente);
    }

    private String catchMessage(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (BusinessException e) {
            return e.getMessage();
        }
    }
}
