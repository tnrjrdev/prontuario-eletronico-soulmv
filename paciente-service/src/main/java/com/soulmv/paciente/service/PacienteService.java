package com.soulmv.paciente.service;

import com.soulmv.paciente.dto.request.PacienteRequest;
import com.soulmv.paciente.dto.response.PacienteResponse;
import com.soulmv.paciente.entity.Paciente;
import com.soulmv.paciente.enums.Sexo;
import com.soulmv.paciente.exception.BusinessException;
import com.soulmv.paciente.exception.ResourceNotFoundException;
import com.soulmv.paciente.mapper.PacienteMapper;
import com.soulmv.paciente.repository.PacienteRepository;
import com.soulmv.paciente.repository.spec.PacienteSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negócio do cadastro de pacientes.
 */
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper mapper;

    public PacienteService(PacienteRepository pacienteRepository,
                           PacienteMapper mapper) {
        this.pacienteRepository = pacienteRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<PacienteResponse> listar(String nome, String cpf, Long convenioId, Pageable pageable) {
        Specification<Paciente> spec = Specification
                .where(PacienteSpecs.nomeContem(nome))
                .and(PacienteSpecs.cpfIgual(cpf))
                .and(PacienteSpecs.convenioId(convenioId));
        return pacienteRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscarPorId(Long id) {
        return mapper.toResponse(obter(id));
    }

    @Transactional
    public PacienteResponse criar(PacienteRequest request) {
        if (pacienteRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("Já existe um paciente cadastrado com este CPF.", HttpStatus.CONFLICT);
        }

        Paciente paciente = Paciente.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .cartaoSus(request.cartaoSus())
                .dataNascimento(request.dataNascimento())
                .sexo(request.sexo() != null ? request.sexo() : Sexo.NAO_INFORMADO)
                .telefone(request.telefone())
                .email(request.email())
                .endereco(mapper.toEntity(request.endereco()))
                .convenioId(request.convenioId())
                .numeroCarteirinha(request.numeroCarteirinha())
                .build();

        return mapper.toResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public PacienteResponse atualizar(Long id, PacienteRequest request) {
        Paciente paciente = obter(id);

        if (!paciente.getCpf().equals(request.cpf()) && pacienteRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("Já existe um paciente cadastrado com este CPF.", HttpStatus.CONFLICT);
        }

        paciente.setNome(request.nome());
        paciente.setCpf(request.cpf());
        paciente.setCartaoSus(request.cartaoSus());
        paciente.setDataNascimento(request.dataNascimento());
        paciente.setSexo(request.sexo() != null ? request.sexo() : Sexo.NAO_INFORMADO);
        paciente.setTelefone(request.telefone());
        paciente.setEmail(request.email());
        paciente.setEndereco(mapper.toEntity(request.endereco()));
        paciente.setConvenioId(request.convenioId());
        paciente.setNumeroCarteirinha(request.numeroCarteirinha());

        return mapper.toResponse(pacienteRepository.save(paciente));
    }


    private Paciente obter(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
    }
}
