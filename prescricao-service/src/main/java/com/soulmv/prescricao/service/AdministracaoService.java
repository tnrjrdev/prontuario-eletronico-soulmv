package com.soulmv.prescricao.service;

import com.soulmv.prescricao.dto.request.AdministracaoRequest;
import com.soulmv.prescricao.dto.response.AdministracaoResponse;
import com.soulmv.prescricao.entity.AdministracaoMedicamento;
import com.soulmv.prescricao.entity.ItemPrescricao;
import com.soulmv.prescricao.enums.StatusPrescricao;
import com.soulmv.prescricao.exception.BusinessException;
import com.soulmv.prescricao.exception.ResourceNotFoundException;
import com.soulmv.prescricao.mapper.AdministracaoMapper;
import com.soulmv.prescricao.repository.AdministracaoMedicamentoRepository;
import com.soulmv.prescricao.repository.ItemPrescricaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdministracaoService {

    private final AdministracaoMedicamentoRepository repository;
    private final ItemPrescricaoRepository itemRepository;
    private final AdministracaoMapper mapper;

    public AdministracaoService(AdministracaoMedicamentoRepository repository,
                                ItemPrescricaoRepository itemRepository,
                                AdministracaoMapper mapper) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.mapper = mapper;
    }

    @Transactional
    public AdministracaoResponse registrar(Long itemId, AdministracaoRequest request, Long enfermeiroId, String enfermeiroNome) {
        ItemPrescricao item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de prescrição", itemId));

        if (item.getPrescricao().getStatus() != StatusPrescricao.ATIVA) {
            throw new BusinessException("A prescrição não está ativa; não é possível checar a medicação.");
        }

        AdministracaoMedicamento administracao = AdministracaoMedicamento.builder()
                .itemPrescricao(item)
                .enfermeiroId(enfermeiroId)
                .enfermeiroNome(enfermeiroNome)
                .status(request.status())
                .dataHoraAdministracao(LocalDateTime.now())
                .observacao(request.observacao())
                .build();

        return mapper.toResponse(repository.save(administracao));
    }

    @Transactional(readOnly = true)
    public List<AdministracaoResponse> listar(Long itemId) {
        return repository.findByItemPrescricaoIdOrderByDataHoraAdministracaoDesc(itemId)
                .stream().map(mapper::toResponse).toList();
    }
}
