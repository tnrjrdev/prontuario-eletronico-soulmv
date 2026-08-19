package com.soulmv.exames.repository;

import com.soulmv.exames.entity.SolicitacaoExame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoExameRepository extends JpaRepository<SolicitacaoExame, Long>, JpaSpecificationExecutor<SolicitacaoExame> {

    List<SolicitacaoExame> findByAtendimentoIdOrderByDataSolicitacaoDesc(Long atendimentoId);
}
