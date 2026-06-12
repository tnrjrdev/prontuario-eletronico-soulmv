package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.AdministracaoResponse;
import com.soulmv.hospitalar.dto.response.AnamneseResponse;
import com.soulmv.hospitalar.dto.response.DiagnosticoResponse;
import com.soulmv.hospitalar.dto.response.ItemPrescricaoResponse;
import com.soulmv.hospitalar.dto.response.PrescricaoResponse;
import com.soulmv.hospitalar.dto.response.ResultadoExameResponse;
import com.soulmv.hospitalar.dto.response.SolicitacaoExameResponse;
import com.soulmv.hospitalar.entity.AdministracaoMedicamento;
import com.soulmv.hospitalar.entity.Anamnese;
import com.soulmv.hospitalar.entity.Anexo;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.Cid10;
import com.soulmv.hospitalar.entity.Diagnostico;
import com.soulmv.hospitalar.entity.ItemPrescricao;
import com.soulmv.hospitalar.entity.Medicamento;
import com.soulmv.hospitalar.entity.Prescricao;
import com.soulmv.hospitalar.entity.ResultadoExame;
import com.soulmv.hospitalar.entity.SolicitacaoExame;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.enums.StatusAdministracao;
import com.soulmv.hospitalar.enums.StatusExame;
import com.soulmv.hospitalar.enums.StatusPrescricao;
import com.soulmv.hospitalar.enums.TipoDiagnostico;
import com.soulmv.hospitalar.enums.ViaAdministracao;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-11T22:31:58-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class ClinicoMapperImpl implements ClinicoMapper {

    @Override
    public AnamneseResponse toResponse(Anamnese anamnese) {
        if ( anamnese == null ) {
            return null;
        }

        Long atendimentoId = null;
        Long medicoId = null;
        String medicoNome = null;
        Long id = null;
        String historiaDoencaAtual = null;
        String antecedentes = null;
        String alergias = null;
        String exameFisico = null;
        LocalDateTime dataHora = null;

        atendimentoId = anamneseAtendimentoId( anamnese );
        medicoId = anamneseMedicoId( anamnese );
        medicoNome = anamneseMedicoNomeCompleto( anamnese );
        id = anamnese.getId();
        historiaDoencaAtual = anamnese.getHistoriaDoencaAtual();
        antecedentes = anamnese.getAntecedentes();
        alergias = anamnese.getAlergias();
        exameFisico = anamnese.getExameFisico();
        dataHora = anamnese.getDataHora();

        AnamneseResponse anamneseResponse = new AnamneseResponse( id, atendimentoId, medicoId, medicoNome, historiaDoencaAtual, antecedentes, alergias, exameFisico, dataHora );

        return anamneseResponse;
    }

    @Override
    public DiagnosticoResponse toResponse(Diagnostico diagnostico) {
        if ( diagnostico == null ) {
            return null;
        }

        Long atendimentoId = null;
        Long cid10Id = null;
        String cid10Codigo = null;
        String cid10Descricao = null;
        Long medicoId = null;
        String medicoNome = null;
        Long id = null;
        TipoDiagnostico tipo = null;
        String observacao = null;
        LocalDateTime dataHora = null;

        atendimentoId = diagnosticoAtendimentoId( diagnostico );
        cid10Id = diagnosticoCid10Id( diagnostico );
        cid10Codigo = diagnosticoCid10Codigo( diagnostico );
        cid10Descricao = diagnosticoCid10Descricao( diagnostico );
        medicoId = diagnosticoMedicoId( diagnostico );
        medicoNome = diagnosticoMedicoNomeCompleto( diagnostico );
        id = diagnostico.getId();
        tipo = diagnostico.getTipo();
        observacao = diagnostico.getObservacao();
        dataHora = diagnostico.getDataHora();

        DiagnosticoResponse diagnosticoResponse = new DiagnosticoResponse( id, atendimentoId, cid10Id, cid10Codigo, cid10Descricao, tipo, medicoId, medicoNome, observacao, dataHora );

        return diagnosticoResponse;
    }

    @Override
    public PrescricaoResponse toResponse(Prescricao prescricao) {
        if ( prescricao == null ) {
            return null;
        }

        Long atendimentoId = null;
        Long medicoId = null;
        String medicoNome = null;
        Long id = null;
        StatusPrescricao status = null;
        String observacao = null;
        LocalDateTime dataHora = null;
        List<ItemPrescricaoResponse> itens = null;

        atendimentoId = prescricaoAtendimentoId( prescricao );
        medicoId = prescricaoMedicoId( prescricao );
        medicoNome = prescricaoMedicoNomeCompleto( prescricao );
        id = prescricao.getId();
        status = prescricao.getStatus();
        observacao = prescricao.getObservacao();
        dataHora = prescricao.getDataHora();
        itens = itemPrescricaoListToItemPrescricaoResponseList( prescricao.getItens() );

        PrescricaoResponse prescricaoResponse = new PrescricaoResponse( id, atendimentoId, medicoId, medicoNome, status, observacao, dataHora, itens );

        return prescricaoResponse;
    }

    @Override
    public ItemPrescricaoResponse toResponse(ItemPrescricao item) {
        if ( item == null ) {
            return null;
        }

        Long medicamentoId = null;
        String medicamentoNome = null;
        boolean medicamentoControlado = false;
        Long id = null;
        String dose = null;
        ViaAdministracao via = null;
        String frequencia = null;
        String duracao = null;
        String observacao = null;

        medicamentoId = itemMedicamentoId( item );
        medicamentoNome = itemMedicamentoNome( item );
        medicamentoControlado = itemMedicamentoControlado( item );
        id = item.getId();
        dose = item.getDose();
        via = item.getVia();
        frequencia = item.getFrequencia();
        duracao = item.getDuracao();
        observacao = item.getObservacao();

        ItemPrescricaoResponse itemPrescricaoResponse = new ItemPrescricaoResponse( id, medicamentoId, medicamentoNome, medicamentoControlado, dose, via, frequencia, duracao, observacao );

        return itemPrescricaoResponse;
    }

    @Override
    public AdministracaoResponse toResponse(AdministracaoMedicamento administracao) {
        if ( administracao == null ) {
            return null;
        }

        Long itemPrescricaoId = null;
        String medicamentoNome = null;
        Long enfermeiroId = null;
        String enfermeiroNome = null;
        Long id = null;
        StatusAdministracao status = null;
        LocalDateTime dataHoraAdministracao = null;
        String observacao = null;

        itemPrescricaoId = administracaoItemPrescricaoId( administracao );
        medicamentoNome = administracaoItemPrescricaoMedicamentoNome( administracao );
        enfermeiroId = administracaoEnfermeiroId( administracao );
        enfermeiroNome = administracaoEnfermeiroNomeCompleto( administracao );
        id = administracao.getId();
        status = administracao.getStatus();
        dataHoraAdministracao = administracao.getDataHoraAdministracao();
        observacao = administracao.getObservacao();

        AdministracaoResponse administracaoResponse = new AdministracaoResponse( id, itemPrescricaoId, medicamentoNome, enfermeiroId, enfermeiroNome, status, dataHoraAdministracao, observacao );

        return administracaoResponse;
    }

    @Override
    public SolicitacaoExameResponse toResponse(SolicitacaoExame solicitacao) {
        if ( solicitacao == null ) {
            return null;
        }

        Long atendimentoId = null;
        Long medicoSolicitanteId = null;
        String medicoSolicitanteNome = null;
        Long id = null;
        String tipoExame = null;
        StatusExame status = null;
        String observacao = null;
        LocalDateTime dataSolicitacao = null;
        ResultadoExameResponse resultado = null;

        atendimentoId = solicitacaoAtendimentoId( solicitacao );
        medicoSolicitanteId = solicitacaoMedicoSolicitanteId( solicitacao );
        medicoSolicitanteNome = solicitacaoMedicoSolicitanteNomeCompleto( solicitacao );
        id = solicitacao.getId();
        tipoExame = solicitacao.getTipoExame();
        status = solicitacao.getStatus();
        observacao = solicitacao.getObservacao();
        dataSolicitacao = solicitacao.getDataSolicitacao();
        resultado = toResponse( solicitacao.getResultado() );

        SolicitacaoExameResponse solicitacaoExameResponse = new SolicitacaoExameResponse( id, atendimentoId, tipoExame, status, observacao, medicoSolicitanteId, medicoSolicitanteNome, dataSolicitacao, resultado );

        return solicitacaoExameResponse;
    }

    @Override
    public ResultadoExameResponse toResponse(ResultadoExame resultado) {
        if ( resultado == null ) {
            return null;
        }

        Long laudoAnexoId = null;
        Long liberadoPorId = null;
        String liberadoPorNome = null;
        Long id = null;
        String resultadoTexto = null;
        LocalDateTime dataLiberacao = null;

        laudoAnexoId = resultadoLaudoId( resultado );
        liberadoPorId = resultadoLiberadoPorId( resultado );
        liberadoPorNome = resultadoLiberadoPorNomeCompleto( resultado );
        id = resultado.getId();
        resultadoTexto = resultado.getResultadoTexto();
        dataLiberacao = resultado.getDataLiberacao();

        boolean temLaudo = resultado.getLaudo() != null;

        ResultadoExameResponse resultadoExameResponse = new ResultadoExameResponse( id, resultadoTexto, laudoAnexoId, temLaudo, liberadoPorId, liberadoPorNome, dataLiberacao );

        return resultadoExameResponse;
    }

    private Long anamneseAtendimentoId(Anamnese anamnese) {
        Atendimento atendimento = anamnese.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        return atendimento.getId();
    }

    private Long anamneseMedicoId(Anamnese anamnese) {
        Usuario medico = anamnese.getMedico();
        if ( medico == null ) {
            return null;
        }
        return medico.getId();
    }

    private String anamneseMedicoNomeCompleto(Anamnese anamnese) {
        Usuario medico = anamnese.getMedico();
        if ( medico == null ) {
            return null;
        }
        return medico.getNomeCompleto();
    }

    private Long diagnosticoAtendimentoId(Diagnostico diagnostico) {
        Atendimento atendimento = diagnostico.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        return atendimento.getId();
    }

    private Long diagnosticoCid10Id(Diagnostico diagnostico) {
        Cid10 cid10 = diagnostico.getCid10();
        if ( cid10 == null ) {
            return null;
        }
        return cid10.getId();
    }

    private String diagnosticoCid10Codigo(Diagnostico diagnostico) {
        Cid10 cid10 = diagnostico.getCid10();
        if ( cid10 == null ) {
            return null;
        }
        return cid10.getCodigo();
    }

    private String diagnosticoCid10Descricao(Diagnostico diagnostico) {
        Cid10 cid10 = diagnostico.getCid10();
        if ( cid10 == null ) {
            return null;
        }
        return cid10.getDescricao();
    }

    private Long diagnosticoMedicoId(Diagnostico diagnostico) {
        Usuario medico = diagnostico.getMedico();
        if ( medico == null ) {
            return null;
        }
        return medico.getId();
    }

    private String diagnosticoMedicoNomeCompleto(Diagnostico diagnostico) {
        Usuario medico = diagnostico.getMedico();
        if ( medico == null ) {
            return null;
        }
        return medico.getNomeCompleto();
    }

    private Long prescricaoAtendimentoId(Prescricao prescricao) {
        Atendimento atendimento = prescricao.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        return atendimento.getId();
    }

    private Long prescricaoMedicoId(Prescricao prescricao) {
        Usuario medico = prescricao.getMedico();
        if ( medico == null ) {
            return null;
        }
        return medico.getId();
    }

    private String prescricaoMedicoNomeCompleto(Prescricao prescricao) {
        Usuario medico = prescricao.getMedico();
        if ( medico == null ) {
            return null;
        }
        return medico.getNomeCompleto();
    }

    protected List<ItemPrescricaoResponse> itemPrescricaoListToItemPrescricaoResponseList(List<ItemPrescricao> list) {
        if ( list == null ) {
            return null;
        }

        List<ItemPrescricaoResponse> list1 = new ArrayList<ItemPrescricaoResponse>( list.size() );
        for ( ItemPrescricao itemPrescricao : list ) {
            list1.add( toResponse( itemPrescricao ) );
        }

        return list1;
    }

    private Long itemMedicamentoId(ItemPrescricao itemPrescricao) {
        Medicamento medicamento = itemPrescricao.getMedicamento();
        if ( medicamento == null ) {
            return null;
        }
        return medicamento.getId();
    }

    private String itemMedicamentoNome(ItemPrescricao itemPrescricao) {
        Medicamento medicamento = itemPrescricao.getMedicamento();
        if ( medicamento == null ) {
            return null;
        }
        return medicamento.getNome();
    }

    private boolean itemMedicamentoControlado(ItemPrescricao itemPrescricao) {
        Medicamento medicamento = itemPrescricao.getMedicamento();
        if ( medicamento == null ) {
            return false;
        }
        return medicamento.isControlado();
    }

    private Long administracaoItemPrescricaoId(AdministracaoMedicamento administracaoMedicamento) {
        ItemPrescricao itemPrescricao = administracaoMedicamento.getItemPrescricao();
        if ( itemPrescricao == null ) {
            return null;
        }
        return itemPrescricao.getId();
    }

    private String administracaoItemPrescricaoMedicamentoNome(AdministracaoMedicamento administracaoMedicamento) {
        ItemPrescricao itemPrescricao = administracaoMedicamento.getItemPrescricao();
        if ( itemPrescricao == null ) {
            return null;
        }
        Medicamento medicamento = itemPrescricao.getMedicamento();
        if ( medicamento == null ) {
            return null;
        }
        return medicamento.getNome();
    }

    private Long administracaoEnfermeiroId(AdministracaoMedicamento administracaoMedicamento) {
        Usuario enfermeiro = administracaoMedicamento.getEnfermeiro();
        if ( enfermeiro == null ) {
            return null;
        }
        return enfermeiro.getId();
    }

    private String administracaoEnfermeiroNomeCompleto(AdministracaoMedicamento administracaoMedicamento) {
        Usuario enfermeiro = administracaoMedicamento.getEnfermeiro();
        if ( enfermeiro == null ) {
            return null;
        }
        return enfermeiro.getNomeCompleto();
    }

    private Long solicitacaoAtendimentoId(SolicitacaoExame solicitacaoExame) {
        Atendimento atendimento = solicitacaoExame.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        return atendimento.getId();
    }

    private Long solicitacaoMedicoSolicitanteId(SolicitacaoExame solicitacaoExame) {
        Usuario medicoSolicitante = solicitacaoExame.getMedicoSolicitante();
        if ( medicoSolicitante == null ) {
            return null;
        }
        return medicoSolicitante.getId();
    }

    private String solicitacaoMedicoSolicitanteNomeCompleto(SolicitacaoExame solicitacaoExame) {
        Usuario medicoSolicitante = solicitacaoExame.getMedicoSolicitante();
        if ( medicoSolicitante == null ) {
            return null;
        }
        return medicoSolicitante.getNomeCompleto();
    }

    private Long resultadoLaudoId(ResultadoExame resultadoExame) {
        Anexo laudo = resultadoExame.getLaudo();
        if ( laudo == null ) {
            return null;
        }
        return laudo.getId();
    }

    private Long resultadoLiberadoPorId(ResultadoExame resultadoExame) {
        Usuario liberadoPor = resultadoExame.getLiberadoPor();
        if ( liberadoPor == null ) {
            return null;
        }
        return liberadoPor.getId();
    }

    private String resultadoLiberadoPorNomeCompleto(ResultadoExame resultadoExame) {
        Usuario liberadoPor = resultadoExame.getLiberadoPor();
        if ( liberadoPor == null ) {
            return null;
        }
        return liberadoPor.getNomeCompleto();
    }
}
