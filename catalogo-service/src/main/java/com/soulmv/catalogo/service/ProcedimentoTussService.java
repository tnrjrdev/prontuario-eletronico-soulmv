package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.ProcedimentoTussRequest;
import com.soulmv.catalogo.dto.response.ProcedimentoTussResponse;
import com.soulmv.catalogo.entity.ProcedimentoTuss;
import com.soulmv.catalogo.exception.BusinessException;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.ProcedimentoTussRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProcedimentoTussService {

    private final ProcedimentoTussRepository repository;
    private final ParametroMapper mapper;

    public ProcedimentoTussService(ProcedimentoTussRepository repository, ParametroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<ProcedimentoTussResponse> listar(String q, Pageable pageable) {
        Page<ProcedimentoTuss> page = StringUtils.hasText(q)
                ? repository.findByCodigoTussContainingIgnoreCaseOrDescricaoContainingIgnoreCase(q, q, pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProcedimentoTussResponse buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public ProcedimentoTussResponse criar(ProcedimentoTussRequest request) {
        if (repository.existsByCodigoTuss(request.codigoTuss())) {
            throw new BusinessException("Já existe um procedimento com este código TUSS.", HttpStatus.CONFLICT);
        }
        ProcedimentoTuss procedimento = ProcedimentoTuss.builder()
                .codigoTuss(request.codigoTuss())
                .descricao(request.descricao())
                .valorReferencia(request.valorReferencia())
                .ativo(true)
                .build();
        return mapper.toResponse(repository.save(procedimento));
    }

    @Transactional
    public ProcedimentoTussResponse atualizar(Long id, ProcedimentoTussRequest request) {
        ProcedimentoTuss procedimento = obter(id);
        if (!procedimento.getCodigoTuss().equalsIgnoreCase(request.codigoTuss())
                && repository.existsByCodigoTuss(request.codigoTuss())) {
            throw new BusinessException("Já existe um procedimento com este código TUSS.", HttpStatus.CONFLICT);
        }
        procedimento.setCodigoTuss(request.codigoTuss());
        procedimento.setDescricao(request.descricao());
        procedimento.setValorReferencia(request.valorReferencia());
        return mapper.toResponse(repository.save(procedimento));
    }

    @Transactional
    public ProcedimentoTussResponse atualizarStatus(Long id, AtualizarStatusRequest request) {
        ProcedimentoTuss procedimento = obter(id);
        procedimento.setAtivo(request.ativo());
        return mapper.toResponse(repository.save(procedimento));
    }

    private ProcedimentoTuss obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Procedimento TUSS", id));
    }
}
