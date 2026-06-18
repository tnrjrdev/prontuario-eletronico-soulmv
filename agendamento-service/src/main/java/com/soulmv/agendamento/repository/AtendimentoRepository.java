package com.soulmv.agendamento.repository;

import com.soulmv.agendamento.entity.Atendimento;
import com.soulmv.agendamento.enums.StatusAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long>,
        JpaSpecificationExecutor<Atendimento> {

    long countByStatus(StatusAtendimento status);
}
