package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.Cid10;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Cid10Repository extends JpaRepository<Cid10, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    Page<Cid10> findByCodigoContainingIgnoreCaseOrDescricaoContainingIgnoreCase(
            String codigo, String descricao, Pageable pageable);
}
