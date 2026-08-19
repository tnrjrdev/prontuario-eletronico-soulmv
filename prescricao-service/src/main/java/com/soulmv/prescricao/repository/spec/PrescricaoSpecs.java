package com.soulmv.prescricao.repository.spec;

import com.soulmv.prescricao.entity.Prescricao;
import com.soulmv.prescricao.enums.StatusPrescricao;
import org.springframework.data.jpa.domain.Specification;

public class PrescricaoSpecs {

    private PrescricaoSpecs() {
    }

    public static Specification<Prescricao> status(StatusPrescricao status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Prescricao> pacienteId(Long pacienteId) {
        return (root, query, cb) -> pacienteId == null ? null : cb.equal(root.get("pacienteId"), pacienteId);
    }
}
