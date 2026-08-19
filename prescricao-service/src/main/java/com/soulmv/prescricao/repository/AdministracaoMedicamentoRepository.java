package com.soulmv.prescricao.repository;

import com.soulmv.prescricao.entity.AdministracaoMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdministracaoMedicamentoRepository extends JpaRepository<AdministracaoMedicamento, Long> {

    List<AdministracaoMedicamento> findByItemPrescricaoIdOrderByDataHoraAdministracaoDesc(Long itemPrescricaoId);
}
