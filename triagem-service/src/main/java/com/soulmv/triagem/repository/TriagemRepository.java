package com.soulmv.triagem.repository;

import com.soulmv.triagem.entity.Triagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    Optional<Triagem> findByAtendimentoId(Long atendimentoId);

    boolean existsByAtendimentoId(Long atendimentoId);
}
