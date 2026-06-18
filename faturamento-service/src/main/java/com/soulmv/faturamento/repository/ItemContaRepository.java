package com.soulmv.faturamento.repository;

import com.soulmv.faturamento.entity.ItemConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemContaRepository extends JpaRepository<ItemConta, Long> {
}
