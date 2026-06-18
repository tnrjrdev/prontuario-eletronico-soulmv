package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.LeitoRequest;
import com.soulmv.catalogo.dto.request.LeitoStatusRequest;
import com.soulmv.catalogo.dto.response.LeitoResponse;
import com.soulmv.catalogo.entity.Leito;
import com.soulmv.catalogo.entity.Setor;
import com.soulmv.catalogo.exception.BusinessException;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.LeitoRepository;
import com.soulmv.catalogo.repository.SetorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeitoService {

    private final LeitoRepository repository;
    private final SetorRepository setorRepository;
    private final ParametroMapper mapper;

    public LeitoService(LeitoRepository repository, SetorRepository setorRepository, ParametroMapper mapper) {
        this.repository = repository;
        this.setorRepository = setorRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<LeitoResponse> listar(Long setorId, Pageable pageable) {
        Page<Leito> page = setorId != null
                ? repository.findBySetorId(setorId, pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public LeitoResponse buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public LeitoResponse criar(LeitoRequest request) {
        Setor setor = obterSetor(request.setorId());
        validarIdentificadorUnico(setor.getId(), request.identificador());

        Leito leito = Leito.builder()
                .identificador(request.identificador())
                .setor(setor)
                .status(com.soulmv.catalogo.enums.StatusLeito.LIVRE)
                .ativo(true)
                .build();
        return mapper.toResponse(repository.save(leito));
    }

    @Transactional
    public LeitoResponse atualizar(Long id, LeitoRequest request) {
        Leito leito = obter(id);
        Setor setor = obterSetor(request.setorId());

        boolean mudouChave = !leito.getSetor().getId().equals(setor.getId())
                || !leito.getIdentificador().equalsIgnoreCase(request.identificador());
        if (mudouChave) {
            validarIdentificadorUnico(setor.getId(), request.identificador());
        }

        leito.setIdentificador(request.identificador());
        leito.setSetor(setor);
        return mapper.toResponse(repository.save(leito));
    }

    @Transactional
    public LeitoResponse atualizarStatus(Long id, LeitoStatusRequest request) {
        Leito leito = obter(id);
        leito.setStatus(request.status());
        return mapper.toResponse(repository.save(leito));
    }

    @Transactional
    public LeitoResponse atualizarAtivo(Long id, AtualizarStatusRequest request) {
        Leito leito = obter(id);
        leito.setAtivo(request.ativo());
        return mapper.toResponse(repository.save(leito));
    }

    private void validarIdentificadorUnico(Long setorId, String identificador) {
        if (repository.existsBySetorIdAndIdentificadorIgnoreCase(setorId, identificador)) {
            throw new BusinessException("Já existe um leito com este identificador neste setor.",
                    HttpStatus.CONFLICT);
        }
    }

    private Leito obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leito", id));
    }

    private Setor obterSetor(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setor", id));
    }
}
