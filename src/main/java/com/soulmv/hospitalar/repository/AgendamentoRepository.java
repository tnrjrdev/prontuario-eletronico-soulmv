package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.Agendamento;
import com.soulmv.hospitalar.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>,
        JpaSpecificationExecutor<Agendamento> {

    /** Agendamentos de um profissional, em dado intervalo, restritos a certos status. */
    List<Agendamento> findByProfissionalIdAndStatusInAndDataHoraBetween(
            Long profissionalId,
            Collection<StatusAgendamento> status,
            LocalDateTime inicio,
            LocalDateTime fim);
}
