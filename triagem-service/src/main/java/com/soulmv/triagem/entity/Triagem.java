package com.soulmv.triagem.entity;

import com.soulmv.triagem.enums.ClassificacaoRisco;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Triagem com classificação de risco (Manchester). Relação 1:1 com o atendimento
 * (dono: atendimento-service — validado via Feign, não relação JPA local).
 */
@Entity
@Table(name = "triagens", uniqueConstraints = @UniqueConstraint(columnNames = "atendimento_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Triagem extends BaseEntity {

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "enfermeiro_id", nullable = false)
    private Long enfermeiroId;

    private String enfermeiroNome;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClassificacaoRisco classificacaoRisco;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
