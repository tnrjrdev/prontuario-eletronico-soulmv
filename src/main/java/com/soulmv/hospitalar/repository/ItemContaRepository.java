package com.soulmv.hospitalar.repository;

import com.soulmv.hospitalar.entity.ItemConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemContaRepository extends JpaRepository<ItemConta, Long> {
}
