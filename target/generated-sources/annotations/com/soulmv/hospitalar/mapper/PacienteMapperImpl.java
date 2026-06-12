package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.request.EnderecoDto;
import com.soulmv.hospitalar.dto.response.PacienteResponse;
import com.soulmv.hospitalar.entity.Convenio;
import com.soulmv.hospitalar.entity.Endereco;
import com.soulmv.hospitalar.entity.Paciente;
import com.soulmv.hospitalar.enums.Sexo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-11T22:31:59-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class PacienteMapperImpl implements PacienteMapper {

    @Override
    public PacienteResponse toResponse(Paciente paciente) {
        if ( paciente == null ) {
            return null;
        }

        Long convenioId = null;
        String convenioNome = null;
        Long id = null;
        String nome = null;
        String cpf = null;
        String cartaoSus = null;
        LocalDate dataNascimento = null;
        Sexo sexo = null;
        String telefone = null;
        String email = null;
        EnderecoDto endereco = null;
        String numeroCarteirinha = null;
        LocalDateTime criadoEm = null;
        LocalDateTime atualizadoEm = null;

        convenioId = pacienteConvenioId( paciente );
        convenioNome = pacienteConvenioNome( paciente );
        id = paciente.getId();
        nome = paciente.getNome();
        cpf = paciente.getCpf();
        cartaoSus = paciente.getCartaoSus();
        dataNascimento = paciente.getDataNascimento();
        sexo = paciente.getSexo();
        telefone = paciente.getTelefone();
        email = paciente.getEmail();
        endereco = toDto( paciente.getEndereco() );
        numeroCarteirinha = paciente.getNumeroCarteirinha();
        criadoEm = paciente.getCriadoEm();
        atualizadoEm = paciente.getAtualizadoEm();

        PacienteResponse pacienteResponse = new PacienteResponse( id, nome, cpf, cartaoSus, dataNascimento, sexo, telefone, email, endereco, convenioId, convenioNome, numeroCarteirinha, criadoEm, atualizadoEm );

        return pacienteResponse;
    }

    @Override
    public EnderecoDto toDto(Endereco endereco) {
        if ( endereco == null ) {
            return null;
        }

        String logradouro = null;
        String numero = null;
        String complemento = null;
        String bairro = null;
        String cidade = null;
        String uf = null;
        String cep = null;

        logradouro = endereco.getLogradouro();
        numero = endereco.getNumero();
        complemento = endereco.getComplemento();
        bairro = endereco.getBairro();
        cidade = endereco.getCidade();
        uf = endereco.getUf();
        cep = endereco.getCep();

        EnderecoDto enderecoDto = new EnderecoDto( logradouro, numero, complemento, bairro, cidade, uf, cep );

        return enderecoDto;
    }

    @Override
    public Endereco toEntity(EnderecoDto dto) {
        if ( dto == null ) {
            return null;
        }

        Endereco.EnderecoBuilder endereco = Endereco.builder();

        endereco.logradouro( dto.logradouro() );
        endereco.numero( dto.numero() );
        endereco.complemento( dto.complemento() );
        endereco.bairro( dto.bairro() );
        endereco.cidade( dto.cidade() );
        endereco.uf( dto.uf() );
        endereco.cep( dto.cep() );

        return endereco.build();
    }

    private Long pacienteConvenioId(Paciente paciente) {
        Convenio convenio = paciente.getConvenio();
        if ( convenio == null ) {
            return null;
        }
        return convenio.getId();
    }

    private String pacienteConvenioNome(Paciente paciente) {
        Convenio convenio = paciente.getConvenio();
        if ( convenio == null ) {
            return null;
        }
        return convenio.getNome();
    }
}
