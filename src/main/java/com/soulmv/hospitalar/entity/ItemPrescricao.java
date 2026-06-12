package com.soulmv.hospitalar.entity;

import com.soulmv.hospitalar.enums.ViaAdministracao;
import jakarta.persistence.Column;
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

/**
 * Item de uma prescrição: medicamento + posologia.
 */
@Entity
@Table(name = "itens_prescricao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPrescricao extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescricao_id", nullable = false)
    private Prescricao prescricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    /** Dose por administração (ex.: "500mg", "1 comprimido"). */
    @Column(nullable = false)
    private String dose;

    @Enumerated(EnumType.STRING)
    private ViaAdministracao via;

    /** Frequência (ex.: "8/8h", "1x ao dia"). */
    private String frequencia;

    /** Duração do tratamento (ex.: "7 dias"). */
    private String duracao;

    private String observacao;
}
