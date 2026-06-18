package com.soulmv.agendamento.repository.spec;

import com.soulmv.agendamento.entity.Agendamento;
import com.soulmv.agendamento.enums.StatusAgendamento;
import com.soulmv.agendamento.enums.TipoAgendamento;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Specifications para filtros dinâmicos na listagem de agendamentos.
 */
public final class AgendamentoSpecs {

    private AgendamentoSpecs() {
    }

    public static Specification<Agendamento> profissionalId(Long profissionalId) {
        if (profissionalId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("profissional").get("id"), profissionalId);
    }

    public static Specification<Agendamento> pacienteId(Long pacienteId) {
        if (pacienteId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("paciente").get("id"), pacienteId);
    }

    public static Specification<Agendamento> setorId(Long setorId) {
        if (setorId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("setor").get("id"), setorId);
    }

    public static Specification<Agendamento> status(StatusAgendamento status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Agendamento> tipo(TipoAgendamento tipo) {
        if (tipo == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Agendamento> dataHoraApartirDe(LocalDateTime de) {
        if (de == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataHora"), de);
    }

    public static Specification<Agendamento> dataHoraAte(LocalDateTime ate) {
        if (ate == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataHora"), ate);
    }
}
