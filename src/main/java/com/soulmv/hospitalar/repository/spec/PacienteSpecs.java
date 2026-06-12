package com.soulmv.hospitalar.repository.spec;

import com.soulmv.hospitalar.entity.Paciente;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Specifications para filtros dinâmicos na listagem de pacientes.
 */
public final class PacienteSpecs {

    private PacienteSpecs() {
    }

    public static Specification<Paciente> nomeContem(String nome) {
        if (!StringUtils.hasText(nome)) {
            return null;
        }
        String like = "%" + nome.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nome")), like);
    }

    public static Specification<Paciente> cpfIgual(String cpf) {
        if (!StringUtils.hasText(cpf)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("cpf"), cpf);
    }

    public static Specification<Paciente> convenioId(Long convenioId) {
        if (convenioId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("convenio").get("id"), convenioId);
    }
}
