package com.soulmv.hospitalar.entity;

import com.soulmv.hospitalar.enums.ClassificacaoRisco;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Triagem com classificação de risco (Manchester). Relação 1:1 com o atendimento.
 */
@Entity
@Table(name = "triagens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Triagem extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false, unique = true)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enfermeiro_id", nullable = false)
    private Usuario enfermeiro;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClassificacaoRisco classificacaoRisco;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
