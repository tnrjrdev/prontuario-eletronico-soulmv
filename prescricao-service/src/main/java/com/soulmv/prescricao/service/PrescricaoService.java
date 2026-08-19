package com.soulmv.prescricao.service;

import com.soulmv.prescricao.client.AtendimentoDto;
import com.soulmv.prescricao.client.MedicamentoDto;
import com.soulmv.prescricao.dto.request.ItemPrescricaoRequest;
import com.soulmv.prescricao.dto.request.PrescricaoRequest;
import com.soulmv.prescricao.dto.request.PrescricaoStatusRequest;
import com.soulmv.prescricao.dto.response.PrescricaoResponse;
import com.soulmv.prescricao.entity.ItemPrescricao;
import com.soulmv.prescricao.entity.Prescricao;
import com.soulmv.prescricao.enums.StatusPrescricao;
import com.soulmv.prescricao.exception.BusinessException;
import com.soulmv.prescricao.exception.ResourceNotFoundException;
import com.soulmv.prescricao.mapper.PrescricaoMapper;
import com.soulmv.prescricao.repository.PrescricaoRepository;
import com.soulmv.prescricao.repository.spec.PrescricaoSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrescricaoService {

    private final PrescricaoRepository repository;
    private final AtendimentoLookupService atendimentoLookup;
    private final MedicamentoLookupService medicamentoLookup;
    private final PrescricaoMapper mapper;

    public PrescricaoService(PrescricaoRepository repository,
                             AtendimentoLookupService atendimentoLookup,
                             MedicamentoLookupService medicamentoLookup,
                             PrescricaoMapper mapper) {
        this.repository = repository;
        this.atendimentoLookup = atendimentoLookup;
        this.medicamentoLookup = medicamentoLookup;
        this.mapper = mapper;
    }

    @Transactional
    public PrescricaoResponse criar(Long atendimentoId, PrescricaoRequest request, Long medicoId, String medicoNome) {
        AtendimentoDto atendimento = atendimentoLookup.buscar(atendimentoId);
        if (atendimento.status().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível prescrever.");
        }

        Prescricao prescricao = Prescricao.builder()
                .atendimentoId(atendimentoId)
                .pacienteId(atendimento.pacienteId())
                .pacienteNome(atendimento.pacienteNome())
                .medicoId(medicoId)
                .medicoNome(medicoNome)
                .status(StatusPrescricao.ATIVA)
                .observacao(request.observacao())
                .dataHora(LocalDateTime.now())
                .build();

        for (ItemPrescricaoRequest itemRequest : request.itens()) {
            MedicamentoDto medicamento = medicamentoLookup.buscar(itemRequest.medicamentoId());
            if (!medicamento.ativo()) {
                throw new BusinessException("Medicamento inativo: " + medicamento.nome());
            }
            ItemPrescricao item = ItemPrescricao.builder()
                    .medicamentoId(medicamento.id())
                    .medicamentoNome(medicamento.nome())
                    .medicamentoControlado(medicamento.controlado())
                    .dose(itemRequest.dose())
                    .via(itemRequest.via())
                    .frequencia(itemRequest.frequencia())
                    .duracao(itemRequest.duracao())
                    .observacao(itemRequest.observacao())
                    .build();
            prescricao.addItem(item);
        }

        return mapper.toResponse(repository.save(prescricao));
    }

    @Transactional(readOnly = true)
    public List<PrescricaoResponse> listar(Long atendimentoId) {
        return repository.findByAtendimentoIdOrderByDataHoraDesc(atendimentoId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<PrescricaoResponse> listarTodas(StatusPrescricao status, Long pacienteId, Pageable pageable) {
        Specification<Prescricao> spec = Specification.where(PrescricaoSpecs.status(status))
                .and(PrescricaoSpecs.pacienteId(pacienteId));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional
    public PrescricaoResponse atualizarStatus(Long id, PrescricaoStatusRequest request) {
        Prescricao prescricao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescrição", id));
        prescricao.setStatus(request.status());
        return mapper.toResponse(repository.save(prescricao));
    }
}
