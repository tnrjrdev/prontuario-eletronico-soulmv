package com.soulmv.exames.repository.spec;

import com.soulmv.exames.entity.SolicitacaoExame;
import com.soulmv.exames.enums.StatusExame;
import org.springframework.data.jpa.domain.Specification;

public class ExameSpecs {

    private ExameSpecs() {
    }

    public static Specification<SolicitacaoExame> status(StatusExame status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<SolicitacaoExame> pacienteId(Long pacienteId) {
        return (root, query, cb) -> pacienteId == null ? null : cb.equal(root.get("pacienteId"), pacienteId);
    }
}
