package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.Setor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetorRepository extends JpaRepository<Setor, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    Page<Setor> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
