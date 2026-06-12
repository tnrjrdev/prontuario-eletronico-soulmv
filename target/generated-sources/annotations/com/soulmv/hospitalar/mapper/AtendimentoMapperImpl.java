package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.AtendimentoResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import com.soulmv.hospitalar.entity.Leito;
import com.soulmv.hospitalar.entity.Paciente;
import com.soulmv.hospitalar.entity.Setor;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.enums.StatusAtendimento;
import com.soulmv.hospitalar.enums.TipoAtendimento;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-11T22:31:59-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class AtendimentoMapperImpl implements AtendimentoMapper {

    @Override
    public AtendimentoResponse toResponse(Atendimento atendimento) {
        if ( atendimento == null ) {
            return null;
        }

        Long pacienteId = null;
        String pacienteNome = null;
        Long setorId = null;
        String setorNome = null;
        Long leitoId = null;
        String leitoIdentificador = null;
        Long profissionalId = null;
        String profissionalNome = null;
        Long id = null;
        TipoAtendimento tipo = null;
        StatusAtendimento status = null;
        String queixaPrincipal = null;
        LocalDateTime dataEntrada = null;
        LocalDateTime dataAlta = null;
        LocalDateTime criadoEm = null;
        LocalDateTime atualizadoEm = null;

        pacienteId = atendimentoPacienteId( atendimento );
        pacienteNome = atendimentoPacienteNome( atendimento );
        setorId = atendimentoSetorId( atendimento );
        setorNome = atendimentoSetorNome( atendimento );
        leitoId = atendimentoLeitoId( atendimento );
        leitoIdentificador = atendimentoLeitoIdentificador( atendimento );
        profissionalId = atendimentoProfissionalResponsavelId( atendimento );
        profissionalNome = atendimentoProfissionalResponsavelNomeCompleto( atendimento );
        id = atendimento.getId();
        tipo = atendimento.getTipo();
        status = atendimento.getStatus();
        queixaPrincipal = atendimento.getQueixaPrincipal();
        dataEntrada = atendimento.getDataEntrada();
        dataAlta = atendimento.getDataAlta();
        criadoEm = atendimento.getCriadoEm();
        atualizadoEm = atendimento.getAtualizadoEm();

        AtendimentoResponse atendimentoResponse = new AtendimentoResponse( id, pacienteId, pacienteNome, tipo, status, setorId, setorNome, leitoId, leitoIdentificador, profissionalId, profissionalNome, queixaPrincipal, dataEntrada, dataAlta, criadoEm, atualizadoEm );

        return atendimentoResponse;
    }

    private Long atendimentoPacienteId(Atendimento atendimento) {
        Paciente paciente = atendimento.getPaciente();
        if ( paciente == null ) {
            return null;
        }
        return paciente.getId();
    }

    private String atendimentoPacienteNome(Atendimento atendimento) {
        Paciente paciente = atendimento.getPaciente();
        if ( paciente == null ) {
            return null;
        }
        return paciente.getNome();
    }

    private Long atendimentoSetorId(Atendimento atendimento) {
        Setor setor = atendimento.getSetor();
        if ( setor == null ) {
            return null;
        }
        return setor.getId();
    }

    private String atendimentoSetorNome(Atendimento atendimento) {
        Setor setor = atendimento.getSetor();
        if ( setor == null ) {
            return null;
        }
        return setor.getNome();
    }

    private Long atendimentoLeitoId(Atendimento atendimento) {
        Leito leito = atendimento.getLeito();
        if ( leito == null ) {
            return null;
        }
        return leito.getId();
    }

    private String atendimentoLeitoIdentificador(Atendimento atendimento) {
        Leito leito = atendimento.getLeito();
        if ( leito == null ) {
            return null;
        }
        return leito.getIdentificador();
    }

    private Long atendimentoProfissionalResponsavelId(Atendimento atendimento) {
        Usuario profissionalResponsavel = atendimento.getProfissionalResponsavel();
        if ( profissionalResponsavel == null ) {
            return null;
        }
        return profissionalResponsavel.getId();
    }

    private String atendimentoProfissionalResponsavelNomeCompleto(Atendimento atendimento) {
        Usuario profissionalResponsavel = atendimento.getProfissionalResponsavel();
        if ( profissionalResponsavel == null ) {
            return null;
        }
        return profissionalResponsavel.getNomeCompleto();
    }
}
