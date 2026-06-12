package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.ContaResponse;
import com.soulmv.hospitalar.dto.response.GuiaTissResponse;
import com.soulmv.hospitalar.dto.response.ItemContaResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.ContaHospitalar;
import com.soulmv.hospitalar.entity.Convenio;
import com.soulmv.hospitalar.entity.GuiaTiss;
import com.soulmv.hospitalar.entity.ItemConta;
import com.soulmv.hospitalar.entity.Paciente;
import com.soulmv.hospitalar.entity.ProcedimentoTuss;
import com.soulmv.hospitalar.enums.StatusConta;
import com.soulmv.hospitalar.enums.StatusGuiaTiss;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-11T22:31:59-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class FaturamentoMapperImpl implements FaturamentoMapper {

    @Override
    public ContaResponse toResponse(ContaHospitalar conta) {
        if ( conta == null ) {
            return null;
        }

        Long atendimentoId = null;
        Long pacienteId = null;
        String pacienteNome = null;
        Long convenioId = null;
        String convenioNome = null;
        Long id = null;
        StatusConta status = null;
        BigDecimal valorTotal = null;
        LocalDateTime dataFechamento = null;
        List<ItemContaResponse> itens = null;
        LocalDateTime criadoEm = null;

        atendimentoId = contaAtendimentoId( conta );
        pacienteId = contaAtendimentoPacienteId( conta );
        pacienteNome = contaAtendimentoPacienteNome( conta );
        convenioId = contaConvenioId( conta );
        convenioNome = contaConvenioNome( conta );
        id = conta.getId();
        status = conta.getStatus();
        valorTotal = conta.getValorTotal();
        dataFechamento = conta.getDataFechamento();
        itens = itemContaListToItemContaResponseList( conta.getItens() );
        criadoEm = conta.getCriadoEm();

        ContaResponse contaResponse = new ContaResponse( id, atendimentoId, pacienteId, pacienteNome, convenioId, convenioNome, status, valorTotal, dataFechamento, itens, criadoEm );

        return contaResponse;
    }

    @Override
    public ItemContaResponse toResponse(ItemConta item) {
        if ( item == null ) {
            return null;
        }

        Long procedimentoId = null;
        String codigoTuss = null;
        String descricao = null;
        Long id = null;
        int quantidade = 0;
        BigDecimal valorUnitario = null;
        BigDecimal valorTotal = null;

        procedimentoId = itemProcedimentoId( item );
        codigoTuss = itemProcedimentoCodigoTuss( item );
        descricao = itemProcedimentoDescricao( item );
        id = item.getId();
        quantidade = item.getQuantidade();
        valorUnitario = item.getValorUnitario();
        valorTotal = item.getValorTotal();

        ItemContaResponse itemContaResponse = new ItemContaResponse( id, procedimentoId, codigoTuss, descricao, quantidade, valorUnitario, valorTotal );

        return itemContaResponse;
    }

    @Override
    public GuiaTissResponse toResponse(GuiaTiss guia) {
        if ( guia == null ) {
            return null;
        }

        Long contaId = null;
        Long id = null;
        String numeroGuia = null;
        StatusGuiaTiss status = null;
        LocalDateTime dataGeracao = null;

        contaId = guiaContaId( guia );
        id = guia.getId();
        numeroGuia = guia.getNumeroGuia();
        status = guia.getStatus();
        dataGeracao = guia.getDataGeracao();

        GuiaTissResponse guiaTissResponse = new GuiaTissResponse( id, contaId, numeroGuia, status, dataGeracao );

        return guiaTissResponse;
    }

    private Long contaAtendimentoId(ContaHospitalar contaHospitalar) {
        Atendimento atendimento = contaHospitalar.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        return atendimento.getId();
    }

    private Long contaAtendimentoPacienteId(ContaHospitalar contaHospitalar) {
        Atendimento atendimento = contaHospitalar.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        Paciente paciente = atendimento.getPaciente();
        if ( paciente == null ) {
            return null;
        }
        return paciente.getId();
    }

    private String contaAtendimentoPacienteNome(ContaHospitalar contaHospitalar) {
        Atendimento atendimento = contaHospitalar.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        Paciente paciente = atendimento.getPaciente();
        if ( paciente == null ) {
            return null;
        }
        return paciente.getNome();
    }

    private Long contaConvenioId(ContaHospitalar contaHospitalar) {
        Convenio convenio = contaHospitalar.getConvenio();
        if ( convenio == null ) {
            return null;
        }
        return convenio.getId();
    }

    private String contaConvenioNome(ContaHospitalar contaHospitalar) {
        Convenio convenio = contaHospitalar.getConvenio();
        if ( convenio == null ) {
            return null;
        }
        return convenio.getNome();
    }

    protected List<ItemContaResponse> itemContaListToItemContaResponseList(List<ItemConta> list) {
        if ( list == null ) {
            return null;
        }

        List<ItemContaResponse> list1 = new ArrayList<ItemContaResponse>( list.size() );
        for ( ItemConta itemConta : list ) {
            list1.add( toResponse( itemConta ) );
        }

        return list1;
    }

    private Long itemProcedimentoId(ItemConta itemConta) {
        ProcedimentoTuss procedimento = itemConta.getProcedimento();
        if ( procedimento == null ) {
            return null;
        }
        return procedimento.getId();
    }

    private String itemProcedimentoCodigoTuss(ItemConta itemConta) {
        ProcedimentoTuss procedimento = itemConta.getProcedimento();
        if ( procedimento == null ) {
            return null;
        }
        return procedimento.getCodigoTuss();
    }

    private String itemProcedimentoDescricao(ItemConta itemConta) {
        ProcedimentoTuss procedimento = itemConta.getProcedimento();
        if ( procedimento == null ) {
            return null;
        }
        return procedimento.getDescricao();
    }

    private Long guiaContaId(GuiaTiss guiaTiss) {
        ContaHospitalar conta = guiaTiss.getConta();
        if ( conta == null ) {
            return null;
        }
        return conta.getId();
    }
}
