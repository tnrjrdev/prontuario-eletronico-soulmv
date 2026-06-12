package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.ItemPrescricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPrescricaoRepository extends JpaRepository<ItemPrescricao, Long> {
}
