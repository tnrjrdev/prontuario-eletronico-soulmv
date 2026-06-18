package com.soulmv.auditoria.repository;

import com.soulmv.auditoria.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long>,
        JpaSpecificationExecutor<LogAuditoria> {
}
