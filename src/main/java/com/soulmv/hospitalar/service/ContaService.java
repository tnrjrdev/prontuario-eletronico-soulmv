package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.ContaRequest;
import com.soulmv.hospitalar.dto.request.ItemContaRequest;
import com.soulmv.hospitalar.dto.response.ContaResponse;
import com.soulmv.hospitalar.dto.response.GuiaTissResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.ContaHospitalar;
import com.soulmv.hospitalar.entity.GuiaTiss;
import com.soulmv.hospitalar.entity.ItemConta;
import com.soulmv.hospitalar.entity.ProcedimentoTuss;
import com.soulmv.hospitalar.enums.StatusConta;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.FaturamentoMapper;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.ContaHospitalarRepository;
import com.soulmv.hospitalar.repository.GuiaTissRepository;
import com.soulmv.hospitalar.repository.ItemContaRepository;
import com.soulmv.hospitalar.repository.ProcedimentoTussRepository;
import com.soulmv.hospitalar.service.faturamento.TissXmlBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContaService {

    private final ContaHospitalarRepository repository;
    private final AtendimentoRepository atendimentoRepository;
    private final ProcedimentoTussRepository procedimentoRepository;
    private final ItemContaRepository itemContaRepository;
    private final GuiaTissRepository guiaRepository;
    private final FaturamentoMapper mapper;
    private final TissXmlBuilder tissXmlBuilder;

    public ContaService(ContaHospitalarRepository repository,
                        AtendimentoRepository atendimentoRepository,
                        ProcedimentoTussRepository procedimentoRepository,
                        ItemContaRepository itemContaRepository,
                        GuiaTissRepository guiaRepository,
                        FaturamentoMapper mapper,
                        TissXmlBuilder tissXmlBuilder) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.itemContaRepository = itemContaRepository;
        this.guiaRepository = guiaRepository;
        this.mapper = mapper;
        this.tissXmlBuilder = tissXmlBuilder;
    }

    @Transactional
    public ContaResponse abrir(ContaRequest request) {
        Atendimento atendimento = atendimentoRepository.findById(request.atendimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", request.atendimentoId()));
        if (repository.existsByAtendimentoId(atendimento.getId())) {
            throw new BusinessException("Este atendimento já possui conta.", HttpStatus.CONFLICT);
        }

        ContaHospitalar conta = ContaHospitalar.builder()
                .atendimento(atendimento)
                .convenio(atendimento.getPaciente().getConvenio())
                .status(StatusConta.ABERTA)
                .valorTotal(BigDecimal.ZERO)
                .build();
        return mapper.toResponse(repository.save(conta));
    }

    @Transactional(readOnly = true)
    public Page<ContaResponse> listar(StatusConta status, Pageable pageable) {
        Page<ContaHospitalar> page = status != null
                ? repository.findByStatus(status, pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public ContaResponse adicionarItem(Long contaId, ItemContaRequest request) {
        ContaHospitalar conta = obter(contaId);
        if (!conta.getStatus().permiteEdicaoItens()) {
            throw new BusinessException("A conta não está aberta para edição de itens.", HttpStatus.CONFLICT);
        }
        ProcedimentoTuss procedimento = procedimentoRepository.findById(request.procedimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Procedimento TUSS", request.procedimentoId()));
        if (!procedimento.isAtivo()) {
            throw new BusinessException("Procedimento inativo: " + procedimento.getCodigoTuss());
        }

        BigDecimal valorUnitario = request.valorUnitario() != null
                ? request.valorUnitario() : procedimento.getValorReferencia();
        if (valorUnitario == null) {
            throw new BusinessException("Informe o valor unitário (o procedimento não tem valor de referência).");
        }

        ItemConta item = ItemConta.builder()
                .conta(conta)
                .procedimento(procedimento)
                .quantidade(request.quantidade())
                .valorUnitario(valorUnitario)
                .build();
        item.calcularTotal();
        itemContaRepository.save(item);

        conta.getItens().add(item);
        conta.recalcularTotal();
        return mapper.toResponse(repository.save(conta));
    }

    @Transactional
    public ContaResponse fechar(Long contaId) {
        ContaHospitalar conta = obter(contaId);
        if (conta.getStatus() != StatusConta.ABERTA) {
            throw new BusinessException("Apenas contas abertas podem ser fechadas.");
        }
        if (conta.getItens().isEmpty()) {
            throw new BusinessException("Não é possível fechar uma conta sem itens.");
        }
        conta.setStatus(StatusConta.FECHADA);
        conta.setDataFechamento(LocalDateTime.now());
        return mapper.toResponse(repository.save(conta));
    }

    @Transactional
    public GuiaTissResponse gerarGuiaTiss(Long contaId) {
        ContaHospitalar conta = obter(contaId);
        if (conta.getStatus() != StatusConta.FECHADA && conta.getStatus() != StatusConta.FATURADA) {
            throw new BusinessException("A conta precisa estar FECHADA para gerar a guia TISS.");
        }

        String numeroGuia = String.format("%010d", guiaRepository.count() + 1);
        LocalDate hoje = LocalDate.now();
        String xml = tissXmlBuilder.gerar(conta, numeroGuia, hoje);

        GuiaTiss guia = GuiaTiss.builder()
                .conta(conta)
                .numeroGuia(numeroGuia)
                .xml(xml)
                .dataGeracao(LocalDateTime.now())
                .build();
        guia = guiaRepository.save(guia);

        conta.setStatus(StatusConta.FATURADA);
        repository.save(conta);

        return mapper.toResponse(guia);
    }

    @Transactional(readOnly = true)
    public List<GuiaTissResponse> listarGuias(Long contaId) {
        obter(contaId);
        return guiaRepository.findByContaIdOrderByDataGeracaoDesc(contaId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public String obterXmlGuia(Long guiaId) {
        return guiaRepository.findById(guiaId)
                .map(GuiaTiss::getXml)
                .orElseThrow(() -> new ResourceNotFoundException("Guia TISS", guiaId));
    }

    private ContaHospitalar obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", id));
    }
}
