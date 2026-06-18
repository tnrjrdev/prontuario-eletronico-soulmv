package com.soulmv.faturamento.repository;

import com.soulmv.faturamento.entity.ProcedimentoTuss;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcedimentoTussRepository extends JpaRepository<ProcedimentoTuss, Long> {

    boolean existsByCodigoTuss(String codigoTuss);

    Page<ProcedimentoTuss> findByCodigoTussContainingIgnoreCaseOrDescricaoContainingIgnoreCase(
            String codigo, String descricao, Pageable pageable);
}
