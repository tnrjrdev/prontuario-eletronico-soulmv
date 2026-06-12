package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.Cid10Request;
import com.soulmv.hospitalar.dto.response.Cid10Response;
import com.soulmv.hospitalar.entity.Cid10;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.ParametroMapper;
import com.soulmv.hospitalar.repository.Cid10Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class Cid10Service {

    private final Cid10Repository repository;
    private final ParametroMapper mapper;

    public Cid10Service(Cid10Repository repository, ParametroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<Cid10Response> listar(String q, Pageable pageable) {
        Page<Cid10> page = StringUtils.hasText(q)
                ? repository.findByCodigoContainingIgnoreCaseOrDescricaoContainingIgnoreCase(q, q, pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Cid10Response buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public Cid10Response criar(Cid10Request request) {
        if (repository.existsByCodigoIgnoreCase(request.codigo())) {
            throw new BusinessException("Já existe uma CID com este código.", HttpStatus.CONFLICT);
        }
        Cid10 cid = Cid10.builder()
                .codigo(request.codigo())
                .descricao(request.descricao())
                .build();
        return mapper.toResponse(repository.save(cid));
    }

    @Transactional
    public Cid10Response atualizar(Long id, Cid10Request request) {
        Cid10 cid = obter(id);
        if (!cid.getCodigo().equalsIgnoreCase(request.codigo())
                && repository.existsByCodigoIgnoreCase(request.codigo())) {
            throw new BusinessException("Já existe uma CID com este código.", HttpStatus.CONFLICT);
        }
        cid.setCodigo(request.codigo());
        cid.setDescricao(request.descricao());
        return mapper.toResponse(repository.save(cid));
    }

    @Transactional
    public void excluir(Long id) {
        Cid10 cid = obter(id);
        repository.delete(cid);
    }

    private Cid10 obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CID-10", id));
    }
}
