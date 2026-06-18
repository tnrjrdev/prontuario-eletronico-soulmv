package com.soulmv.catalogo.service;

import com.soulmv.catalogo.dto.request.AtualizarStatusRequest;
import com.soulmv.catalogo.dto.request.MedicamentoRequest;
import com.soulmv.catalogo.dto.response.MedicamentoResponse;
import com.soulmv.catalogo.entity.Medicamento;
import com.soulmv.catalogo.exception.ResourceNotFoundException;
import com.soulmv.catalogo.mapper.ParametroMapper;
import com.soulmv.catalogo.repository.MedicamentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MedicamentoService {

    private final MedicamentoRepository repository;
    private final ParametroMapper mapper;

    public MedicamentoService(MedicamentoRepository repository, ParametroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<MedicamentoResponse> listar(String q, Pageable pageable) {
        Page<Medicamento> page = StringUtils.hasText(q)
                ? repository.findByNomeContainingIgnoreCase(q, pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MedicamentoResponse buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public MedicamentoResponse criar(MedicamentoRequest request) {
        Medicamento medicamento = Medicamento.builder()
                .nome(request.nome())
                .principioAtivo(request.principioAtivo())
                .concentracao(request.concentracao())
                .controlado(request.controlado())
                .ativo(true)
                .build();
        return mapper.toResponse(repository.save(medicamento));
    }

    @Transactional
    public MedicamentoResponse atualizar(Long id, MedicamentoRequest request) {
        Medicamento medicamento = obter(id);
        medicamento.setNome(request.nome());
        medicamento.setPrincipioAtivo(request.principioAtivo());
        medicamento.setConcentracao(request.concentracao());
        medicamento.setControlado(request.controlado());
        return mapper.toResponse(repository.save(medicamento));
    }

    @Transactional
    public MedicamentoResponse atualizarStatus(Long id, AtualizarStatusRequest request) {
        Medicamento medicamento = obter(id);
        medicamento.setAtivo(request.ativo());
        return mapper.toResponse(repository.save(medicamento));
    }

    private Medicamento obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", id));
    }
}
