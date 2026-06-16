package com.soulmv.hospitalar.repository.spec;

import com.soulmv.hospitalar.entity.SolicitacaoExame;
import com.soulmv.hospitalar.enums.StatusExame;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para a listagem global de exames.
 */
public final class ExameSpecs {

    private ExameSpecs() {
    }

    public static Specification<SolicitacaoExame> status(StatusExame status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<SolicitacaoExame> pacienteId(Long pacienteId) {
        if (pacienteId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("atendimento").get("paciente").get("id"), pacienteId);
    }
}
