package com.soulmv.atendimento.repository.spec;

import com.soulmv.atendimento.entity.Atendimento;
import com.soulmv.atendimento.enums.StatusAtendimento;
import com.soulmv.atendimento.enums.TipoAtendimento;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros dinâmicos na listagem/fila de atendimentos.
 */
public final class AtendimentoSpecs {

    private AtendimentoSpecs() {
    }

    public static Specification<Atendimento> status(StatusAtendimento status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Atendimento> tipo(TipoAtendimento tipo) {
        if (tipo == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Atendimento> setorId(Long setorId) {
        if (setorId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("setorId"), setorId);
    }

    public static Specification<Atendimento> pacienteId(Long pacienteId) {
        if (pacienteId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("pacienteId"), pacienteId);
    }
}
