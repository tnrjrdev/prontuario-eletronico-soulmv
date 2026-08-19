package com.soulmv.anamnese.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Anamnese do atendimento (1:1, dono: atendimento-service — validado via Feign).
 */
@Entity
@Table(name = "anamneses", uniqueConstraints = @UniqueConstraint(columnNames = "atendimento_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anamnese extends BaseEntity {

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    private String medicoNome;

    @Lob
    private String historiaDoencaAtual;

    @Lob
    private String antecedentes;

    private String alergias;

    @Lob
    private String exameFisico;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
