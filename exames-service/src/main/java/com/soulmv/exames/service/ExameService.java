package com.soulmv.exames.service;

import com.soulmv.exames.client.AtendimentoDto;
import com.soulmv.exames.dto.request.ExameStatusRequest;
import com.soulmv.exames.dto.request.SolicitacaoExameRequest;
import com.soulmv.exames.dto.response.SolicitacaoExameResponse;
import com.soulmv.exames.entity.Anexo;
import com.soulmv.exames.entity.ResultadoExame;
import com.soulmv.exames.entity.SolicitacaoExame;
import com.soulmv.exames.enums.StatusExame;
import com.soulmv.exames.exception.BusinessException;
import com.soulmv.exames.exception.ResourceNotFoundException;
import com.soulmv.exames.mapper.ExameMapper;
import com.soulmv.exames.repository.AnexoRepository;
import com.soulmv.exames.repository.SolicitacaoExameRepository;
import com.soulmv.exames.repository.spec.ExameSpecs;
import com.soulmv.exames.service.storage.ArquivoDownload;
import com.soulmv.exames.service.storage.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExameService {

    private final SolicitacaoExameRepository repository;
    private final AnexoRepository anexoRepository;
    private final AtendimentoLookupService atendimentoLookup;
    private final StorageService storageService;
    private final ExameMapper mapper;

    public ExameService(SolicitacaoExameRepository repository,
                        AnexoRepository anexoRepository,
                        AtendimentoLookupService atendimentoLookup,
                        StorageService storageService,
                        ExameMapper mapper) {
        this.repository = repository;
        this.anexoRepository = anexoRepository;
        this.atendimentoLookup = atendimentoLookup;
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Transactional
    public SolicitacaoExameResponse solicitar(Long atendimentoId, SolicitacaoExameRequest request,
                                              Long medicoId, String medicoNome) {
        AtendimentoDto atendimento = atendimentoLookup.buscar(atendimentoId);
        if (atendimento.status().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível solicitar exames.");
        }
        SolicitacaoExame exame = SolicitacaoExame.builder()
                .atendimentoId(atendimentoId)
                .pacienteId(atendimento.pacienteId())
                .pacienteNome(atendimento.pacienteNome())
                .medicoSolicitanteId(medicoId)
                .medicoSolicitanteNome(medicoNome)
                .tipoExame(request.tipoExame())
                .observacao(request.observacao())
                .status(StatusExame.SOLICITADO)
                .dataSolicitacao(LocalDateTime.now())
                .build();
        return mapper.toResponse(repository.save(exame));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoExameResponse> listar(Long atendimentoId) {
        return repository.findByAtendimentoIdOrderByDataSolicitacaoDesc(atendimentoId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoExameResponse> listarTodos(StatusExame status, Long pacienteId, Pageable pageable) {
        Specification<SolicitacaoExame> spec = Specification
                .where(ExameSpecs.status(status))
                .and(ExameSpecs.pacienteId(pacienteId));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional
    public SolicitacaoExameResponse atualizarStatus(Long exameId, ExameStatusRequest request) {
        SolicitacaoExame exame = obter(exameId);
        if (request.status() == StatusExame.LIBERADO) {
            throw new BusinessException("Use o endpoint de resultado para liberar o exame.");
        }
        if (exame.getStatus().isFinal()) {
            throw new BusinessException("Exame já finalizado; não é possível alterar o status.");
        }
        exame.setStatus(request.status());
        return mapper.toResponse(repository.save(exame));
    }

    @Transactional
    public SolicitacaoExameResponse liberarResultado(Long exameId, String resultadoTexto, MultipartFile laudo,
                                                      Long liberadoPorId, String liberadoPorNome) {
        SolicitacaoExame exame = obter(exameId);
        if (exame.getStatus() == StatusExame.CANCELADO) {
            throw new BusinessException("Exame cancelado; não é possível liberar resultado.");
        }
        if (exame.getResultado() != null) {
            throw new BusinessException("Este exame já possui resultado liberado.", HttpStatus.CONFLICT);
        }
        if (!StringUtils.hasText(resultadoTexto) && (laudo == null || laudo.isEmpty())) {
            throw new BusinessException("Informe o texto do resultado e/ou o arquivo do laudo.");
        }

        Anexo anexo = null;
        if (laudo != null && !laudo.isEmpty()) {
            String nomeArmazenado = storageService.armazenar(laudo);
            anexo = anexoRepository.save(Anexo.builder()
                    .nomeOriginal(laudo.getOriginalFilename())
                    .nomeArmazenado(nomeArmazenado)
                    .contentType(laudo.getContentType())
                    .tamanho(laudo.getSize())
                    .enviadoPorId(liberadoPorId)
                    .enviadoPorNome(liberadoPorNome)
                    .build());
        }

        ResultadoExame resultado = ResultadoExame.builder()
                .solicitacao(exame)
                .resultadoTexto(resultadoTexto)
                .laudo(anexo)
                .liberadoPorId(liberadoPorId)
                .liberadoPorNome(liberadoPorNome)
                .dataLiberacao(LocalDateTime.now())
                .build();
        exame.setResultado(resultado);
        exame.setStatus(StatusExame.LIBERADO);
        return mapper.toResponse(repository.save(exame));
    }

    @Transactional(readOnly = true)
    public ArquivoDownload baixarLaudo(Long exameId) {
        SolicitacaoExame exame = obter(exameId);
        ResultadoExame resultado = exame.getResultado();
        if (resultado == null || resultado.getLaudo() == null) {
            throw new ResourceNotFoundException("Laudo do exame", exameId);
        }
        Anexo anexo = resultado.getLaudo();
        return new ArquivoDownload(
                storageService.carregar(anexo.getNomeArmazenado()),
                anexo.getNomeOriginal(),
                anexo.getContentType());
    }

    private SolicitacaoExame obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exame", id));
    }
}
