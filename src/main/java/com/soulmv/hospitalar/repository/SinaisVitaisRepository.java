package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.SinaisVitais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SinaisVitaisRepository extends JpaRepository<SinaisVitais, Long> {

    List<SinaisVitais> findByAtendimentoIdOrderByDataHoraDesc(Long atendimentoId);
}
