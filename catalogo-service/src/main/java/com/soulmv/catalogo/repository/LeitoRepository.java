package com.soulmv.catalogo.repository;

import com.soulmv.catalogo.entity.Leito;
import com.soulmv.catalogo.enums.StatusLeito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeitoRepository extends JpaRepository<Leito, Long> {

    boolean existsBySetorIdAndIdentificadorIgnoreCase(Long setorId, String identificador);

    Page<Leito> findBySetorId(Long setorId, Pageable pageable);

    long countByStatus(StatusLeito status);

    long countByAtivoTrue();
}
