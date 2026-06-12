package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.Convenio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConvenioRepository extends JpaRepository<Convenio, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    Page<Convenio> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
