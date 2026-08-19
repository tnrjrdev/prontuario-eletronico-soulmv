package com.soulmv.exames.repository;

import com.soulmv.exames.entity.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {
}
