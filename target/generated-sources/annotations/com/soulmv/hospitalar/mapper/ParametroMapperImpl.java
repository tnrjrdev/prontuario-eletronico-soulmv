package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.Cid10Response;
import com.soulmv.hospitalar.dto.response.ConvenioResponse;
import com.soulmv.hospitalar.dto.response.LeitoResponse;
import com.soulmv.hospitalar.dto.response.MedicamentoResponse;
import com.soulmv.hospitalar.dto.response.ProcedimentoTussResponse;
import com.soulmv.hospitalar.dto.response.SetorResponse;
import com.soulmv.hospitalar.entity.Cid10;
import com.soulmv.hospitalar.entity.Convenio;
import com.soulmv.hospitalar.entity.Leito;
import com.soulmv.hospitalar.entity.Medicamento;
import com.soulmv.hospitalar.entity.ProcedimentoTuss;
import com.soulmv.hospitalar.entity.Setor;
import com.soulmv.hospitalar.enums.StatusLeito;
import com.soulmv.hospitalar.enums.TipoConvenio;
import com.soulmv.hospitalar.enums.TipoSetor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-11T22:31:59-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class ParametroMapperImpl implements ParametroMapper {

    @Override
    public SetorResponse toResponse(Setor setor) {
        if ( setor == null ) {
            return null;
        }

        Long id = null;
        String nome = null;
        TipoSetor tipo = null;
        String descricao = null;
        boolean ativo = false;
        LocalDateTime criadoEm = null;
        LocalDateTime atualizadoEm = null;

        id = setor.getId();
        nome = setor.getNome();
        tipo = setor.getTipo();
        descricao = setor.getDescricao();
        ativo = setor.isAtivo();
        criadoEm = setor.getCriadoEm();
        atualizadoEm = setor.getAtualizadoEm();

        SetorResponse setorResponse = new SetorResponse( id, nome, tipo, descricao, ativo, criadoEm, atualizadoEm );

        return setorResponse;
    }

    @Override
    public LeitoResponse toResponse(Leito leito) {
        if ( leito == null ) {
            return null;
        }

        Long setorId = null;
        String setorNome = null;
        Long id = null;
        String identificador = null;
        StatusLeito status = null;
        boolean ativo = false;
        LocalDateTime criadoEm = null;
        LocalDateTime atualizadoEm = null;

        setorId = leitoSetorId( leito );
        setorNome = leitoSetorNome( leito );
        id = leito.getId();
        identificador = leito.getIdentificador();
        status = leito.getStatus();
        ativo = leito.isAtivo();
        criadoEm = leito.getCriadoEm();
        atualizadoEm = leito.getAtualizadoEm();

        LeitoResponse leitoResponse = new LeitoResponse( id, identificador, setorId, setorNome, status, ativo, criadoEm, atualizadoEm );

        return leitoResponse;
    }

    @Override
    public ConvenioResponse toResponse(Convenio convenio) {
        if ( convenio == null ) {
            return null;
        }

        Long id = null;
        String nome = null;
        String registroAns = null;
        TipoConvenio tipo = null;
        boolean ativo = false;
        LocalDateTime criadoEm = null;
        LocalDateTime atualizadoEm = null;

        id = convenio.getId();
        nome = convenio.getNome();
        registroAns = convenio.getRegistroAns();
        tipo = convenio.getTipo();
        ativo = convenio.isAtivo();
        criadoEm = convenio.getCriadoEm();
        atualizadoEm = convenio.getAtualizadoEm();

        ConvenioResponse convenioResponse = new ConvenioResponse( id, nome, registroAns, tipo, ativo, criadoEm, atualizadoEm );

        return convenioResponse;
    }

    @Override
    public MedicamentoResponse toResponse(Medicamento medicamento) {
        if ( medicamento == null ) {
            return null;
        }

        Long id = null;
        String nome = null;
        String principioAtivo = null;
        String concentracao = null;
        boolean controlado = false;
        boolean ativo = false;
        LocalDateTime criadoEm = null;
        LocalDateTime atualizadoEm = null;

        id = medicamento.getId();
        nome = medicamento.getNome();
        principioAtivo = medicamento.getPrincipioAtivo();
        concentracao = medicamento.getConcentracao();
        controlado = medicamento.isControlado();
        ativo = medicamento.isAtivo();
        criadoEm = medicamento.getCriadoEm();
        atualizadoEm = medicamento.getAtualizadoEm();

        MedicamentoResponse medicamentoResponse = new MedicamentoResponse( id, nome, principioAtivo, concentracao, controlado, ativo, criadoEm, atualizadoEm );

        return medicamentoResponse;
    }

    @Override
    public ProcedimentoTussResponse toResponse(ProcedimentoTuss procedimento) {
        if ( procedimento == null ) {
            return null;
        }

        Long id = null;
        String codigoTuss = null;
        String descricao = null;
        BigDecimal valorReferencia = null;
        boolean ativo = false;
        LocalDateTime criadoEm = null;
        LocalDateTime atualizadoEm = null;

        id = procedimento.getId();
        codigoTuss = procedimento.getCodigoTuss();
        descricao = procedimento.getDescricao();
        valorReferencia = procedimento.getValorReferencia();
        ativo = procedimento.isAtivo();
        criadoEm = procedimento.getCriadoEm();
        atualizadoEm = procedimento.getAtualizadoEm();

        ProcedimentoTussResponse procedimentoTussResponse = new ProcedimentoTussResponse( id, codigoTuss, descricao, valorReferencia, ativo, criadoEm, atualizadoEm );

        return procedimentoTussResponse;
    }

    @Override
    public Cid10Response toResponse(Cid10 cid10) {
        if ( cid10 == null ) {
            return null;
        }

        Long id = null;
        String codigo = null;
        String descricao = null;

        id = cid10.getId();
        codigo = cid10.getCodigo();
        descricao = cid10.getDescricao();

        Cid10Response cid10Response = new Cid10Response( id, codigo, descricao );

        return cid10Response;
    }

    private Long leitoSetorId(Leito leito) {
        Setor setor = leito.getSetor();
        if ( setor == null ) {
            return null;
        }
        return setor.getId();
    }

    private String leitoSetorNome(Leito leito) {
        Setor setor = leito.getSetor();
        if ( setor == null ) {
            return null;
        }
        return setor.getNome();
    }
}
