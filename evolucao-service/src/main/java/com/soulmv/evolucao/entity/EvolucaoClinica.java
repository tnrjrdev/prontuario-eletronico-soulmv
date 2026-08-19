package com.soulmv.evolucao.entity;

import com.soulmv.evolucao.enums.TipoEvolucao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Evolução clínica do atendimento (dono: atendimento-service, validado via Feign).
 * O {@code tipo} distingue evolução médica e de enfermagem; cada perfil só registra
 * a sua (regra aplicada no controller, a partir da role do JWT).
 */
@Entity
@Table(name = "evolucoes_clinicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvolucaoClinica extends BaseEntity {

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoEvolucao tipo;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    private String autorNome;

    @Lob
    @Column(nullable = false)
    private String texto;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
