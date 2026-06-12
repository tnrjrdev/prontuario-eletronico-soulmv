package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.GuiaTiss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuiaTissRepository extends JpaRepository<GuiaTiss, Long> {

    List<GuiaTiss> findByContaIdOrderByDataGeracaoDesc(Long contaId);

    long count();
}
