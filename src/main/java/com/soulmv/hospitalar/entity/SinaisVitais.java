package com.soulmv.hospitalar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro pontual de sinais vitais de um atendimento (pode haver vários).
 */
@Entity
@Table(name = "sinais_vitais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SinaisVitais extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Usuario registradoPor;

    private Integer pressaoSistolica;
    private Integer pressaoDiastolica;
    private Integer frequenciaCardiaca;
    private Integer frequenciaRespiratoria;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperatura;

    private Integer saturacaoO2;
    private Integer glicemia;

    /** Escala de dor (0 a 10). */
    private Integer escalaDor;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
