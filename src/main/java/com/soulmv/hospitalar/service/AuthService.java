package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.LoginRequest;
import com.soulmv.hospitalar.dto.request.RefreshRequest;
import com.soulmv.hospitalar.dto.response.TokenResponse;
import com.soulmv.hospitalar.dto.response.UsuarioResponse;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.UsuarioMapper;
import com.soulmv.hospitalar.repository.UsuarioRepository;
import com.soulmv.hospitalar.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra autenticação (login), renovação de token (refresh) e consulta do
 * usuário logado (me).
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
            throw new BusinessException("Login ou senha inválidos.", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = usuarioRepository.findByLogin(request.login())
                .orElseThrow(() -> new BusinessException("Login ou senha inválidos.", HttpStatus.UNAUTHORIZED));

        return gerarTokens(usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();
        if (!jwtService.isValido(token) || !jwtService.isRefreshToken(token)) {
            throw new BusinessException("Refresh token inválido ou expirado.", HttpStatus.UNAUTHORIZED);
        }

        String login = jwtService.extrairLogin(token);
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new BusinessException("Refresh token inválido.", HttpStatus.UNAUTHORIZED));

        if (!usuario.isAtivo()) {
            throw new BusinessException("Usuário inativo.", HttpStatus.FORBIDDEN);
        }

        return gerarTokens(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse me(String login) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", login));
        return usuarioMapper.toResponse(usuario);
    }

    private TokenResponse gerarTokens(Usuario usuario) {
        String access = jwtService.gerarAccessToken(usuario);
        String refresh = jwtService.gerarRefreshToken(usuario);
        return TokenResponse.bearer(access, refresh,
                jwtService.getAccessTokenExpiracaoSegundos(), usuarioMapper.toResponse(usuario));
    }
}
