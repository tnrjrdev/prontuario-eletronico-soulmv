package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.enums.StatusAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long>,
        JpaSpecificationExecutor<Atendimento> {

    long countByStatus(StatusAtendimento status);
}
