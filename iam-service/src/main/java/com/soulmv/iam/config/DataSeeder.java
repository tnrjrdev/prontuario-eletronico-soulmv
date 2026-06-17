package com.soulmv.iam.config;

import com.soulmv.iam.entity.Usuario;
import com.soulmv.iam.enums.Role;
import com.soulmv.iam.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Cria um usuário ADMIN inicial caso a base esteja vazia, permitindo o primeiro
 * acesso ao sistema. As credenciais podem ser configuradas por variáveis de
 * ambiente (ADMIN_LOGIN / ADMIN_SENHA / ADMIN_EMAIL).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.login:admin}")
    private String adminLogin;

    @Value("${app.seed.admin.senha:admin123}")
    private String adminSenha;

    @Value("${app.seed.admin.email:admin@soulmv.com}")
    private String adminEmail;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario admin = Usuario.builder()
                .nomeCompleto("Administrador do Sistema")
                .login(adminLogin)
                .email(adminEmail)
                .senhaHash(passwordEncoder.encode(adminSenha))
                .ativo(true)
                .roles(Set.of(Role.ADMIN))
                .build();
        usuarioRepository.save(admin);

        log.warn("==================================================================");
        log.warn(" ADMIN inicial criado -> login: '{}' / senha: '{}'", adminLogin, adminSenha);
        log.warn(" ALTERE esta senha em produção (variáveis ADMIN_LOGIN/ADMIN_SENHA).");
        log.warn("==================================================================");
    }
}
