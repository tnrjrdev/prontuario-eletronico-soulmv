package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long> {

    List<Diagnostico> findByAtendimentoIdOrderByDataHoraDesc(Long atendimentoId);
}
