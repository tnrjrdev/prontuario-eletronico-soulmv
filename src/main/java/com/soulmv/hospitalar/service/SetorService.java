package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.AtualizarStatusRequest;
import com.soulmv.hospitalar.dto.request.SetorRequest;
import com.soulmv.hospitalar.dto.response.SetorResponse;
import com.soulmv.hospitalar.entity.Setor;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.ParametroMapper;
import com.soulmv.hospitalar.repository.SetorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SetorService {

    private final SetorRepository repository;
    private final ParametroMapper mapper;

    public SetorService(SetorRepository repository, ParametroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<SetorResponse> listar(String q, Pageable pageable) {
        Page<Setor> page = StringUtils.hasText(q)
                ? repository.findByNomeContainingIgnoreCase(q, pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SetorResponse buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public SetorResponse criar(SetorRequest request) {
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new BusinessException("Já existe um setor com este nome.", HttpStatus.CONFLICT);
        }
        Setor setor = Setor.builder()
                .nome(request.nome())
                .tipo(request.tipo())
                .descricao(request.descricao())
                .ativo(true)
                .build();
        return mapper.toResponse(repository.save(setor));
    }

    @Transactional
    public SetorResponse atualizar(Long id, SetorRequest request) {
        Setor setor = obter(id);
        if (!setor.getNome().equalsIgnoreCase(request.nome())
                && repository.existsByNomeIgnoreCase(request.nome())) {
            throw new BusinessException("Já existe um setor com este nome.", HttpStatus.CONFLICT);
        }
        setor.setNome(request.nome());
        setor.setTipo(request.tipo());
        setor.setDescricao(request.descricao());
        return mapper.toResponse(repository.save(setor));
    }

    @Transactional
    public SetorResponse atualizarStatus(Long id, AtualizarStatusRequest request) {
        Setor setor = obter(id);
        setor.setAtivo(request.ativo());
        return mapper.toResponse(repository.save(setor));
    }

    private Setor obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setor", id));
    }
}
