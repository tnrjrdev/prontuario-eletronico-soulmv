package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.AtualizarRolesRequest;
import com.soulmv.hospitalar.dto.request.AtualizarStatusRequest;
import com.soulmv.hospitalar.dto.request.UsuarioCreateRequest;
import com.soulmv.hospitalar.dto.request.UsuarioUpdateRequest;
import com.soulmv.hospitalar.dto.response.UsuarioResponse;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.UsuarioMapper;
import com.soulmv.hospitalar.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 * Regras de negócio para gestão de usuários (operações restritas ao ADMIN).
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioMapper usuarioMapper,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(usuarioMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return usuarioMapper.toResponse(obter(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest request) {
        if (usuarioRepository.existsByLogin(request.login())) {
            throw new BusinessException("Já existe um usuário com este login.", HttpStatus.CONFLICT);
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Já existe um usuário com este e-mail.", HttpStatus.CONFLICT);
        }

        Usuario usuario = Usuario.builder()
                .nomeCompleto(request.nomeCompleto())
                .login(request.login())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .ativo(true)
                .roles(new HashSet<>(request.roles()))
                .build();

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = obter(id);

        usuarioRepository.findByEmail(request.email())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new BusinessException("Já existe um usuário com este e-mail.", HttpStatus.CONFLICT);
                });

        usuario.setNomeCompleto(request.nomeCompleto());
        usuario.setEmail(request.email());
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizarStatus(Long id, AtualizarStatusRequest request) {
        Usuario usuario = obter(id);
        usuario.setAtivo(request.ativo());
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizarRoles(Long id, AtualizarRolesRequest request) {
        Usuario usuario = obter(id);
        usuario.setRoles(new HashSet<>(request.roles()));
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    private Usuario obter(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }
}
