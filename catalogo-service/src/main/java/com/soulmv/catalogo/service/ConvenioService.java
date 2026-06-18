package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.ConvenioRequest;
import com.soulmv.catalogo.dto.response.ConvenioResponse;
import com.soulmv.catalogo.entity.Convenio;
import com.soulmv.catalogo.exception.BusinessException;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.ConvenioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ConvenioService {

    private final ConvenioRepository repository;
    private final ParametroMapper mapper;

    public ConvenioService(ConvenioRepository repository, ParametroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<ConvenioResponse> listar(String q, Pageable pageable) {
        Page<Convenio> page = StringUtils.hasText(q)
                ? repository.findByNomeContainingIgnoreCase(q, pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ConvenioResponse buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public ConvenioResponse criar(ConvenioRequest request) {
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new BusinessException("Já existe um convênio com este nome.", HttpStatus.CONFLICT);
        }
        Convenio convenio = Convenio.builder()
                .nome(request.nome())
                .registroAns(request.registroAns())
                .tipo(request.tipo())
                .ativo(true)
                .build();
        return mapper.toResponse(repository.save(convenio));
    }

    @Transactional
    public ConvenioResponse atualizar(Long id, ConvenioRequest request) {
        Convenio convenio = obter(id);
        if (!convenio.getNome().equalsIgnoreCase(request.nome())
                && repository.existsByNomeIgnoreCase(request.nome())) {
            throw new BusinessException("Já existe um convênio com este nome.", HttpStatus.CONFLICT);
        }
        convenio.setNome(request.nome());
        convenio.setRegistroAns(request.registroAns());
        convenio.setTipo(request.tipo());
        return mapper.toResponse(repository.save(convenio));
    }

    @Transactional
    public ConvenioResponse atualizarStatus(Long id, AtualizarStatusRequest request) {
        Convenio convenio = obter(id);
        convenio.setAtivo(request.ativo());
        return mapper.toResponse(repository.save(convenio));
    }

    private Convenio obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convênio", id));
    }
}
