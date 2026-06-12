package com.soulmv.hospitalar.service.support;

import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

/**
 * Resolve a entidade Usuario a partir do login autenticado, reaproveitado pelos
 * serviços clínicos para registrar autoria (enfermeiro, médico, etc.).
 */
@Component
public class UsuarioLookup {

    private final UsuarioRepository usuarioRepository;

    public UsuarioLookup(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario porLogin(String login) {
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", login));
    }
}
