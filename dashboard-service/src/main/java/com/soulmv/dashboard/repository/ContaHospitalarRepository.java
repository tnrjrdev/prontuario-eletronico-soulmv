package com.soulmv.dashboard.repository;

import com.soulmv.dashboard.entity.ContaHospitalar;
import com.soulmv.dashboard.enums.StatusConta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ContaHospitalarRepository extends JpaRepository<ContaHospitalar, Long> {

    boolean existsByAtendimentoId(Long atendimentoId);

    Page<ContaHospitalar> findByStatus(StatusConta status, Pageable pageable);

    long countByStatus(StatusConta status);

    @Query("select coalesce(sum(c.valorTotal), 0) from ContaHospitalar c")
    BigDecimal somaValorTotal();

    @Query("select coalesce(sum(c.valorTotal), 0) from ContaHospitalar c where c.status = ?1")
    BigDecimal somaValorTotalPorStatus(StatusConta status);
}
