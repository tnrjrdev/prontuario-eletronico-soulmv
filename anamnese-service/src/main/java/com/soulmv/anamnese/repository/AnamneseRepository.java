package com.soulmv.anamnese.repository;

import com.soulmv.anamnese.entity.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnamneseRepository extends JpaRepository<Anamnese, Long> {

    Optional<Anamnese> findByAtendimentoId(Long atendimentoId);

    boolean existsByAtendimentoId(Long atendimentoId);
}
