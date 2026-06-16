package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.ItemPrescricaoRequest;
import com.soulmv.hospitalar.dto.request.PrescricaoRequest;
import com.soulmv.hospitalar.dto.request.PrescricaoStatusRequest;
import com.soulmv.hospitalar.dto.response.PrescricaoResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.ItemPrescricao;
import com.soulmv.hospitalar.entity.Medicamento;
import com.soulmv.hospitalar.entity.Prescricao;
import com.soulmv.hospitalar.enums.StatusPrescricao;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.ClinicoMapper;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.MedicamentoRepository;
import com.soulmv.hospitalar.repository.PrescricaoRepository;
import com.soulmv.hospitalar.repository.spec.PrescricaoSpecs;
import com.soulmv.hospitalar.service.support.UsuarioLookup;
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
    private final AtendimentoRepository atendimentoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ClinicoMapper mapper;
    private final UsuarioLookup usuarioLookup;

    public PrescricaoService(PrescricaoRepository repository,
                             AtendimentoRepository atendimentoRepository,
                             MedicamentoRepository medicamentoRepository,
                             ClinicoMapper mapper,
                             UsuarioLookup usuarioLookup) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.mapper = mapper;
        this.usuarioLookup = usuarioLookup;
    }

    @Transactional
    public PrescricaoResponse criar(Long atendimentoId, PrescricaoRequest request, String login) {
        Atendimento atendimento = obterAtendimento(atendimentoId);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível prescrever.");
        }

        Prescricao prescricao = Prescricao.builder()
                .atendimento(atendimento)
                .medico(usuarioLookup.porLogin(login))
                .status(StatusPrescricao.ATIVA)
                .observacao(request.observacao())
                .dataHora(LocalDateTime.now())
                .build();

        for (ItemPrescricaoRequest itemReq : request.itens()) {
            Medicamento medicamento = medicamentoRepository.findById(itemReq.medicamentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicamento", itemReq.medicamentoId()));
            if (!medicamento.isAtivo()) {
                throw new BusinessException("Medicamento inativo: " + medicamento.getNome());
            }
            ItemPrescricao item = ItemPrescricao.builder()
                    .medicamento(medicamento)
                    .dose(itemReq.dose())
                    .via(itemReq.via())
                    .frequencia(itemReq.frequencia())
                    .duracao(itemReq.duracao())
                    .observacao(itemReq.observacao())
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

    /** Listagem global de prescrições (filtros opcionais por status e paciente). */
    @Transactional(readOnly = true)
    public Page<PrescricaoResponse> listarTodas(StatusPrescricao status, Long pacienteId, Pageable pageable) {
        Specification<Prescricao> spec = Specification
                .where(PrescricaoSpecs.status(status))
                .and(PrescricaoSpecs.pacienteId(pacienteId));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional
    public PrescricaoResponse atualizarStatus(Long prescricaoId, PrescricaoStatusRequest request) {
        Prescricao prescricao = repository.findById(prescricaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescrição", prescricaoId));
        prescricao.setStatus(request.status());
        return mapper.toResponse(repository.save(prescricao));
    }

    private Atendimento obterAtendimento(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", id));
    }
}
