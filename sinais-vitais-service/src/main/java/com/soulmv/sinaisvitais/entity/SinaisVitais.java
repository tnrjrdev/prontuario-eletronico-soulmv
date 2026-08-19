package com.soulmv.sinaisvitais.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro pontual de sinais vitais de um atendimento (pode haver vários). Atendimento
 * e quem registrou são donos de outros microsserviços — id + nome denormalizado, sem
 * relação JPA local.
 */
@Entity
@Table(name = "sinais_vitais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SinaisVitais extends BaseEntity {

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "registrado_por_id", nullable = false)
    private Long registradoPorId;

    private String registradoPorNome;

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
