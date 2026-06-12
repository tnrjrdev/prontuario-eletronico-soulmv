package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.EvolucaoClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvolucaoClinicaRepository extends JpaRepository<EvolucaoClinica, Long> {

    List<EvolucaoClinica> findByAtendimentoIdOrderByDataHoraDesc(Long atendimentoId);
}
