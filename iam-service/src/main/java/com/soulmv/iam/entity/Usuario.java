package com.soulmv.iam.entity;

import com.soulmv.iam.enums.Role;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * UsuÃ¡rio do sistema (credencial de acesso).
 *
 * Os vÃ­nculos opcionais com Profissional (corpo clÃ­nico) e Paciente (portal)
 * serÃ£o adicionados como associaÃ§Ãµes nas Etapas 3 e 4. Por enquanto guardamos
 * apenas os identificadores para permitir o data-scoping do portal sem acoplar
 * entidades ainda inexistentes.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity {

    @Column(nullable = false)
    private String nomeCompleto;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /** VÃ­nculo opcional com um profissional (corpo clÃ­nico). Preenchido na Etapa 3. */
    @Column(name = "profissional_id")
    private Long profissionalId;

    /** VÃ­nculo opcional com um paciente (portal). Usado no data-scoping. Etapa 4. */
    @Column(name = "paciente_id")
    private Long pacienteId;
}
