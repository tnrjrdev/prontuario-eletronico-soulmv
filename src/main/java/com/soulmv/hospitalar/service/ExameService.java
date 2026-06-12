package com.soulmv.hospitalar.service;

import com.soulmv.hospitalar.dto.request.ExameStatusRequest;
import com.soulmv.hospitalar.dto.request.SolicitacaoExameRequest;
import com.soulmv.hospitalar.dto.response.SolicitacaoExameResponse;
import com.soulmv.hospitalar.entity.Anexo;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.ResultadoExame;
import com.soulmv.hospitalar.entity.SolicitacaoExame;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.enums.StatusExame;
import com.soulmv.hospitalar.exception.BusinessException;
import com.soulmv.hospitalar.exception.ResourceNotFoundException;
import com.soulmv.hospitalar.mapper.ClinicoMapper;
import com.soulmv.hospitalar.repository.AnexoRepository;
import com.soulmv.hospitalar.repository.AtendimentoRepository;
import com.soulmv.hospitalar.repository.SolicitacaoExameRepository;
import com.soulmv.hospitalar.service.storage.ArquivoDownload;
import com.soulmv.hospitalar.service.storage.StorageService;
import com.soulmv.hospitalar.service.support.UsuarioLookup;
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
    private final AtendimentoRepository atendimentoRepository;
    private final AnexoRepository anexoRepository;
    private final StorageService storageService;
    private final ClinicoMapper mapper;
    private final UsuarioLookup usuarioLookup;

    public ExameService(SolicitacaoExameRepository repository,
                        AtendimentoRepository atendimentoRepository,
                        AnexoRepository anexoRepository,
                        StorageService storageService,
                        ClinicoMapper mapper,
                        UsuarioLookup usuarioLookup) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
        this.anexoRepository = anexoRepository;
        this.storageService = storageService;
        this.mapper = mapper;
        this.usuarioLookup = usuarioLookup;
    }

    @Transactional
    public SolicitacaoExameResponse solicitar(Long atendimentoId, SolicitacaoExameRequest request, String login) {
        Atendimento atendimento = obterAtendimento(atendimentoId);
        if (atendimento.getStatus().isFinal()) {
            throw new BusinessException("Atendimento encerrado; não é possível solicitar exames.");
        }
        SolicitacaoExame exame = SolicitacaoExame.builder()
                .atendimento(atendimento)
                .medicoSolicitante(usuarioLookup.porLogin(login))
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
    public SolicitacaoExameResponse liberarResultado(Long exameId, String resultadoTexto,
                                                     MultipartFile laudo, String login) {
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

        Usuario liberador = usuarioLookup.porLogin(login);

        Anexo anexo = null;
        if (laudo != null && !laudo.isEmpty()) {
            String nomeArmazenado = storageService.armazenar(laudo);
            anexo = anexoRepository.save(Anexo.builder()
                    .nomeOriginal(laudo.getOriginalFilename())
                    .nomeArmazenado(nomeArmazenado)
                    .contentType(laudo.getContentType())
                    .tamanho(laudo.getSize())
                    .enviadoPor(liberador)
                    .build());
        }

        ResultadoExame resultado = ResultadoExame.builder()
                .solicitacao(exame)
                .resultadoTexto(resultadoTexto)
                .laudo(anexo)
                .liberadoPor(liberador)
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

    private Atendimento obterAtendimento(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento", id));
    }
}
