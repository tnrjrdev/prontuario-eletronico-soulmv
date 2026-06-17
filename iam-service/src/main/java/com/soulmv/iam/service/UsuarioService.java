package com.soulmv.iam.service;

import com.soulmv.iam.dto.request.AtualizarRolesRequest;
import com.soulmv.iam.dto.request.AtualizarStatusRequest;
import com.soulmv.iam.dto.request.UsuarioCreateRequest;
import com.soulmv.iam.dto.request.UsuarioUpdateRequest;
import com.soulmv.iam.dto.response.ProfissionalResponse;
import com.soulmv.iam.dto.response.UsuarioResponse;
import com.soulmv.iam.entity.Usuario;
import com.soulmv.iam.enums.Role;
import com.soulmv.iam.exception.BusinessException;
import com.soulmv.iam.exception.ResourceNotFoundException;
import com.soulmv.iam.mapper.UsuarioMapper;
import com.soulmv.iam.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Regras de negÃ³cio para gestÃ£o de usuÃ¡rios (operaÃ§Ãµes restritas ao ADMIN).
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

    /** Profissionais de saÃºde ativos (mÃ©dicos/enfermeiros) para seletores. */
    @Transactional(readOnly = true)
    public List<ProfissionalResponse> listarProfissionais() {
        Set<Role> clinicos = Set.of(Role.MEDICO, Role.ENFERMEIRO);
        return usuarioRepository.findAtivosComRoles(clinicos).stream()
                .map(u -> new ProfissionalResponse(u.getId(), u.getNomeCompleto(), u.getRoles()))
                .toList();
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest request) {
        if (usuarioRepository.existsByLogin(request.login())) {
            throw new BusinessException("JÃ¡ existe um usuÃ¡rio com este login.", HttpStatus.CONFLICT);
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("JÃ¡ existe um usuÃ¡rio com este e-mail.", HttpStatus.CONFLICT);
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
                    throw new BusinessException("JÃ¡ existe um usuÃ¡rio com este e-mail.", HttpStatus.CONFLICT);
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
                .orElseThrow(() -> new ResourceNotFoundException("UsuÃ¡rio", id));
    }
}
