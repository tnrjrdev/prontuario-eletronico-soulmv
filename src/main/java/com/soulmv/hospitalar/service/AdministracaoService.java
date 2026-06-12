package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.AdministracaoRequest;
import com.soulmv.hospitalar.dto.response.AdministracaoResponse;
import com.soulmv.hospitalar.entity.AdministracaoMedicamento;
import com.soulmv.hospitalar.entity.ItemPrescricao;
import com.soulmv.hospitalar.enums.StatusPrescricao;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.ClinicoMapper;
import com.soulmv.hospitalar.repository.AdministracaoMedicamentoRepository;
import com.soulmv.hospitalar.repository.ItemPrescricaoRepository;
import com.soulmv.hospitalar.service.support.UsuarioLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Checagem/administração de itens prescritos pela enfermagem.
 */
@Service
public class AdministracaoService {

    private final AdministracaoMedicamentoRepository repository;
    private final ItemPrescricaoRepository itemRepository;
    private final ClinicoMapper mapper;
    private final UsuarioLookup usuarioLookup;

    public AdministracaoService(AdministracaoMedicamentoRepository repository,
                                ItemPrescricaoRepository itemRepository,
                                ClinicoMapper mapper,
                                UsuarioLookup usuarioLookup) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.mapper = mapper;
        this.usuarioLookup = usuarioLookup;
    }

    @Transactional
    public AdministracaoResponse registrar(Long itemId, AdministracaoRequest request, String login) {
        ItemPrescricao item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de prescrição", itemId));

        if (item.getPrescricao().getStatus() != StatusPrescricao.ATIVA) {
            throw new BusinessException("A prescrição não está ativa; não é possível checar a medicação.");
        }

        AdministracaoMedicamento adm = AdministracaoMedicamento.builder()
                .itemPrescricao(item)
                .enfermeiro(usuarioLookup.porLogin(login))
                .status(request.status())
                .observacao(request.observacao())
                .dataHoraAdministracao(LocalDateTime.now())
                .build();
        return mapper.toResponse(repository.save(adm));
    }

    @Transactional(readOnly = true)
    public List<AdministracaoResponse> listar(Long itemId) {
        return repository.findByItemPrescricaoIdOrderByDataHoraAdministracaoDesc(itemId)
                .stream().map(mapper::toResponse).toList();
    }
}
