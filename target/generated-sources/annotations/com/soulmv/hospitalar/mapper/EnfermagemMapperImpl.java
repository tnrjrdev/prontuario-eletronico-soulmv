package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.EvolucaoResponse;
import com.soulmv.hospitalar.dto.response.SinaisVitaisResponse;
import com.soulmv.hospitalar.dto.response.TriagemResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.EvolucaoClinica;
import com.soulmv.hospitalar.entity.SinaisVitais;
import com.soulmv.hospitalar.entity.Triagem;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.enums.ClassificacaoRisco;
import com.soulmv.hospitalar.enums.TipoEvolucao;
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
public class EnfermagemMapperImpl implements EnfermagemMapper {

    @Override
    public TriagemResponse toResponse(Triagem triagem) {
        if ( triagem == null ) {
            return null;
        }

        Long atendimentoId = null;
        Long enfermeiroId = null;
        String enfermeiroNome = null;
        Long id = null;
        ClassificacaoRisco classificacaoRisco = null;
        String observacao = null;
        LocalDateTime dataHora = null;

        atendimentoId = triagemAtendimentoId( triagem );
        enfermeiroId = triagemEnfermeiroId( triagem );
        enfermeiroNome = triagemEnfermeiroNomeCompleto( triagem );
        id = triagem.getId();
        classificacaoRisco = triagem.getClassificacaoRisco();
        observacao = triagem.getObservacao();
        dataHora = triagem.getDataHora();

        String descricaoRisco = triagem.getClassificacaoRisco().getDescricao();

        TriagemResponse triagemResponse = new TriagemResponse( id, atendimentoId, classificacaoRisco, descricaoRisco, observacao, enfermeiroId, enfermeiroNome, dataHora );

        return triagemResponse;
    }

    @Override
    public SinaisVitaisResponse toResponse(SinaisVitais sinaisVitais) {
        if ( sinaisVitais == null ) {
            return null;
        }

        Long atendimentoId = null;
        Long registradoPorId = null;
        String registradoPorNome = null;
        Long id = null;
        Integer pressaoSistolica = null;
        Integer pressaoDiastolica = null;
        Integer frequenciaCardiaca = null;
        Integer frequenciaRespiratoria = null;
        BigDecimal temperatura = null;
        Integer saturacaoO2 = null;
        Integer glicemia = null;
        Integer escalaDor = null;
        LocalDateTime dataHora = null;

        atendimentoId = sinaisVitaisAtendimentoId( sinaisVitais );
        registradoPorId = sinaisVitaisRegistradoPorId( sinaisVitais );
        registradoPorNome = sinaisVitaisRegistradoPorNomeCompleto( sinaisVitais );
        id = sinaisVitais.getId();
        pressaoSistolica = sinaisVitais.getPressaoSistolica();
        pressaoDiastolica = sinaisVitais.getPressaoDiastolica();
        frequenciaCardiaca = sinaisVitais.getFrequenciaCardiaca();
        frequenciaRespiratoria = sinaisVitais.getFrequenciaRespiratoria();
        temperatura = sinaisVitais.getTemperatura();
        saturacaoO2 = sinaisVitais.getSaturacaoO2();
        glicemia = sinaisVitais.getGlicemia();
        escalaDor = sinaisVitais.getEscalaDor();
        dataHora = sinaisVitais.getDataHora();

        SinaisVitaisResponse sinaisVitaisResponse = new SinaisVitaisResponse( id, atendimentoId, pressaoSistolica, pressaoDiastolica, frequenciaCardiaca, frequenciaRespiratoria, temperatura, saturacaoO2, glicemia, escalaDor, registradoPorId, registradoPorNome, dataHora );

        return sinaisVitaisResponse;
    }

    @Override
    public EvolucaoResponse toResponse(EvolucaoClinica evolucao) {
        if ( evolucao == null ) {
            return null;
        }

        Long atendimentoId = null;
        Long autorId = null;
        String autorNome = null;
        Long id = null;
        TipoEvolucao tipo = null;
        String texto = null;
        LocalDateTime dataHora = null;

        atendimentoId = evolucaoAtendimentoId( evolucao );
        autorId = evolucaoAutorId( evolucao );
        autorNome = evolucaoAutorNomeCompleto( evolucao );
        id = evolucao.getId();
        tipo = evolucao.getTipo();
        texto = evolucao.getTexto();
        dataHora = evolucao.getDataHora();

        EvolucaoResponse evolucaoResponse = new EvolucaoResponse( id, atendimentoId, tipo, texto, autorId, autorNome, dataHora );

        return evolucaoResponse;
    }

    private Long triagemAtendimentoId(Triagem triagem) {
        Atendimento atendimento = triagem.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        return atendimento.getId();
    }

    private Long triagemEnfermeiroId(Triagem triagem) {
        Usuario enfermeiro = triagem.getEnfermeiro();
        if ( enfermeiro == null ) {
            return null;
        }
        return enfermeiro.getId();
    }

    private String triagemEnfermeiroNomeCompleto(Triagem triagem) {
        Usuario enfermeiro = triagem.getEnfermeiro();
        if ( enfermeiro == null ) {
            return null;
        }
        return enfermeiro.getNomeCompleto();
    }

    private Long sinaisVitaisAtendimentoId(SinaisVitais sinaisVitais) {
        Atendimento atendimento = sinaisVitais.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        return atendimento.getId();
    }

    private Long sinaisVitaisRegistradoPorId(SinaisVitais sinaisVitais) {
        Usuario registradoPor = sinaisVitais.getRegistradoPor();
        if ( registradoPor == null ) {
            return null;
        }
        return registradoPor.getId();
    }

    private String sinaisVitaisRegistradoPorNomeCompleto(SinaisVitais sinaisVitais) {
        Usuario registradoPor = sinaisVitais.getRegistradoPor();
        if ( registradoPor == null ) {
            return null;
        }
        return registradoPor.getNomeCompleto();
    }

    private Long evolucaoAtendimentoId(EvolucaoClinica evolucaoClinica) {
        Atendimento atendimento = evolucaoClinica.getAtendimento();
        if ( atendimento == null ) {
            return null;
        }
        return atendimento.getId();
    }

    private Long evolucaoAutorId(EvolucaoClinica evolucaoClinica) {
        Usuario autor = evolucaoClinica.getAutor();
        if ( autor == null ) {
            return null;
        }
        return autor.getId();
    }

    private String evolucaoAutorNomeCompleto(EvolucaoClinica evolucaoClinica) {
        Usuario autor = evolucaoClinica.getAutor();
        if ( autor == null ) {
            return null;
        }
        return autor.getNomeCompleto();
    }
}
