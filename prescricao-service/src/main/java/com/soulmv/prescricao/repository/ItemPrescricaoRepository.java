package com.soulmv.prescricao.repository;

import com.soulmv.prescricao.entity.ItemPrescricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPrescricaoRepository extends JpaRepository<ItemPrescricao, Long> {
}
