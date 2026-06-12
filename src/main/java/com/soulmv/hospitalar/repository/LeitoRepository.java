package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.Leito;
import com.soulmv.hospitalar.enums.StatusLeito;
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
