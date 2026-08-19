package com.soulmv.prescricao.entity;

import com.soulmv.prescricao.enums.ViaAdministracao;
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

    @Column(name = "medicamento_id", nullable = false)
    private Long medicamentoId;

    private String medicamentoNome;

    private boolean medicamentoControlado;

    @Column(nullable = false)
    private String dose;

    @Enumerated(EnumType.STRING)
    private ViaAdministracao via;

    private String frequencia;

    private String duracao;

    private String observacao;
}
