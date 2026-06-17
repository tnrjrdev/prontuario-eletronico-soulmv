package com.soulmv.iam.service;

import com.soulmv.iam.dto.request.LoginRequest;
import com.soulmv.iam.dto.request.RefreshRequest;
import com.soulmv.iam.dto.response.TokenResponse;
import com.soulmv.iam.dto.response.UsuarioResponse;
import com.soulmv.iam.entity.Usuario;
import com.soulmv.iam.exception.BusinessException;
import com.soulmv.iam.exception.ResourceNotFoundException;
import com.soulmv.iam.mapper.UsuarioMapper;
import com.soulmv.iam.repository.UsuarioRepository;
import com.soulmv.iam.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra autenticaÃ§Ã£o (login), renovaÃ§Ã£o de token (refresh) e consulta do
 * usuÃ¡rio logado (me).
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository usuarioRepository,
                       JwtService jwtService,
                       UsuarioMapper usuarioMapper) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.login(), request.senha()));
        } catch (BadCredentialsException e) {
            throw new BusinessException("Login ou senha invÃ¡lidos.", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = usuarioRepository.findByLogin(request.login())
                .orElseThrow(() -> new BusinessException("Login ou senha invÃ¡lidos.", HttpStatus.UNAUTHORIZED));

        return gerarTokens(usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();
        if (!jwtService.isValido(token) || !jwtService.isRefreshToken(token)) {
            throw new BusinessException("Refresh token invÃ¡lido ou expirado.", HttpStatus.UNAUTHORIZED);
        }

        String login = jwtService.extrairLogin(token);
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new BusinessException("Refresh token invÃ¡lido.", HttpStatus.UNAUTHORIZED));

        if (!usuario.isAtivo()) {
            throw new BusinessException("UsuÃ¡rio inativo.", HttpStatus.FORBIDDEN);
        }

        return gerarTokens(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse me(String login) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("UsuÃ¡rio", login));
        return usuarioMapper.toResponse(usuario);
    }

    private TokenResponse gerarTokens(Usuario usuario) {
        String access = jwtService.gerarAccessToken(usuario);
        String refresh = jwtService.gerarRefreshToken(usuario);
        return TokenResponse.bearer(access, refresh,
                jwtService.getAccessTokenExpiracaoSegundos(), usuarioMapper.toResponse(usuario));
    }
}
