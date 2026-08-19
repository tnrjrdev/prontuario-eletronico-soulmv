package com.soulmv.prescricao.repository;

import com.soulmv.prescricao.entity.Prescricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescricaoRepository extends JpaRepository<Prescricao, Long>, JpaSpecificationExecutor<Prescricao> {

    List<Prescricao> findByAtendimentoIdOrderByDataHoraDesc(Long atendimentoId);
}
