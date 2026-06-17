package com.soulmv.paciente.entity;

import com.soulmv.paciente.enums.Sexo;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Paciente — dados demográficos e de convênio. Conteúdo clínico fica em
 * entidades separadas (atendimento, evolução, etc.) a partir da Etapa 5.
 */
@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente extends BaseEntity {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    /** Número do Cartão Nacional de Saúde (CNS/SUS). */
    private String cartaoSus;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Sexo sexo = Sexo.NAO_INFORMADO;

    private String telefone;
    private String email;

    @Embedded
    private Endereco endereco;

    @Column(name = "convenio_id")
    private Long convenioId;

    /** Número da carteirinha no convênio (quando aplicável). */
    private String numeroCarteirinha;
}
