package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.Triagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    Optional<Triagem> findByAtendimentoId(Long atendimentoId);

    boolean existsByAtendimentoId(Long atendimentoId);
}
