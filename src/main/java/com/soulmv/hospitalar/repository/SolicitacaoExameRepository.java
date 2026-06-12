package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.SolicitacaoExame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoExameRepository extends JpaRepository<SolicitacaoExame, Long> {

    List<SolicitacaoExame> findByAtendimentoIdOrderByDataSolicitacaoDesc(Long atendimentoId);
}
