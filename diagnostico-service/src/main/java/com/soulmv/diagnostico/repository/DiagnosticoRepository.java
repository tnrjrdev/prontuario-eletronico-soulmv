package com.soulmv.diagnostico.repository;

import com.soulmv.diagnostico.entity.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long> {

    List<Diagnostico> findByAtendimentoIdOrderByDataHoraDesc(Long atendimentoId);
}
