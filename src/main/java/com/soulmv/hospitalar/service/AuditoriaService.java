package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.response.AuditoriaResponse;
import com.soulmv.hospitalar.entity.LogAuditoria;
import com.soulmv.hospitalar.mapper.AuditoriaMapper;
import com.soulmv.hospitalar.repository.LogAuditoriaRepository;
import com.soulmv.hospitalar.repository.spec.LogAuditoriaSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final LogAuditoriaRepository repository;
    private final AuditoriaMapper mapper;

    public AuditoriaService(LogAuditoriaRepository repository, AuditoriaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Registra um evento na trilha. Usa transação independente para não interferir
     * com a requisição auditada (que já terminou ao chamar este método).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String usuarioLogin, String metodo, String caminho, int status, String ip) {
        LogAuditoria log = LogAuditoria.builder()
                .usuarioLogin(usuarioLogin)
                .metodo(metodo)
                .caminho(caminho)
                .status(status)
                .ip(ip)
                .dataHora(LocalDateTime.now())
                .build();
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> listar(String usuario, String caminho,
                                          LocalDateTime de, LocalDateTime ate, Pageable pageable) {
        Specification<LogAuditoria> spec = Specification
                .where(LogAuditoriaSpecs.usuario(usuario))
                .and(LogAuditoriaSpecs.caminhoContem(caminho))
                .and(LogAuditoriaSpecs.de(de))
                .and(LogAuditoriaSpecs.ate(ate));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }
}
