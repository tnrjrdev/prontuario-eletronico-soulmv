package com.soulmv.hospitalar.entity;

import com.soulmv.hospitalar.enums.TipoEvolucao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Evolução clínica do atendimento. O {@code tipo} distingue evolução médica e
 * de enfermagem; cada perfil só registra a sua (regra aplicada no service).
 */
@Entity
@Table(name = "evolucoes_clinicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvolucaoClinica extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false)
    private Atendimento atendimento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoEvolucao tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Lob
    @Column(nullable = false)
    private String texto;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
