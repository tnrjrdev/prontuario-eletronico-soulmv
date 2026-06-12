package com.soulmv.hospitalar.repository.spec;

import com.soulmv.hospitalar.entity.LogAuditoria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public final class LogAuditoriaSpecs {

    private LogAuditoriaSpecs() {
    }

    public static Specification<LogAuditoria> usuario(String login) {
        if (!StringUtils.hasText(login)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("usuarioLogin")), login.toLowerCase());
    }

    public static Specification<LogAuditoria> caminhoContem(String caminho) {
        if (!StringUtils.hasText(caminho)) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("caminho")), "%" + caminho.toLowerCase() + "%");
    }

    public static Specification<LogAuditoria> de(LocalDateTime de) {
        if (de == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataHora"), de);
    }

    public static Specification<LogAuditoria> ate(LocalDateTime ate) {
        if (ate == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataHora"), ate);
    }
}
