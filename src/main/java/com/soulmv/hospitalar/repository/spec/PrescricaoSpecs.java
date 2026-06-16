package com.soulmv.hospitalar.repository.spec;

import com.soulmv.hospitalar.entity.Prescricao;
import com.soulmv.hospitalar.enums.StatusPrescricao;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para a listagem global de prescrições.
 */
public final class PrescricaoSpecs {

    private PrescricaoSpecs() {
    }

    public static Specification<Prescricao> status(StatusPrescricao status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Prescricao> pacienteId(Long pacienteId) {
        if (pacienteId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("atendimento").get("paciente").get("id"), pacienteId);
    }
}
