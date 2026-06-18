package com.soulmv.dashboard.repository;

import com.soulmv.dashboard.entity.Atendimento;
import com.soulmv.dashboard.enums.StatusAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long>,
        JpaSpecificationExecutor<Atendimento> {

    long countByStatus(StatusAtendimento status);
}
