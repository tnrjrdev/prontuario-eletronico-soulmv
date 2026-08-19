package com.soulmv.diagnostico.entity;

import com.soulmv.diagnostico.enums.TipoDiagnostico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Diagnóstico (CID-10) atribuído pelo médico a um atendimento. Atendimento e CID-10
 * são donos de outros microsserviços (atendimento-service, catalogo-service) —
 * validados via Feign, sem relação JPA local.
 */
@Entity
@Table(name = "diagnosticos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnostico extends BaseEntity {

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "cid10_id", nullable = false)
    private Long cid10Id;

    private String cid10Codigo;

    private String cid10Descricao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoDiagnostico tipo;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    private String medicoNome;

    private String observacao;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
