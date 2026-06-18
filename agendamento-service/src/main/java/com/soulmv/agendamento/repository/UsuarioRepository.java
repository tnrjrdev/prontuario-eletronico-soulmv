package com.soulmv.agendamento.repository;

import com.soulmv.agendamento.entity.Usuario;
import com.soulmv.agendamento.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByLogin(String login);

    Optional<Usuario> findByEmail(String email);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    /** Usuários ativos que possuam pelo menos um dos perfis informados. */
    @Query("select distinct u from Usuario u join u.roles r "
            + "where u.ativo = true and r in :roles order by u.nomeCompleto")
    List<Usuario> findAtivosComRoles(@Param("roles") Collection<Role> roles);
}
