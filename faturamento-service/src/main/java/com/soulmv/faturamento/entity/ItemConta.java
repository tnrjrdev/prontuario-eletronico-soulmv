package com.soulmv.faturamento.entity;

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

/**
 * Item de conta: um procedimento TUSS, com quantidade e valor.
 */
@Entity
@Table(name = "itens_conta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemConta extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private ContaHospitalar conta;

    /**
     * Procedimento TUSS é dono do catalogo-service (não é mais uma relação JPA local).
     * codigoTuss/descricao são um retrato do procedimento no momento em que o item foi
     * faturado — de propósito: se o catálogo renomear ou repreçar o procedimento depois,
     * a conta já fechada não deve mudar retroativamente.
     */
    @Column(nullable = false)
    private Long procedimentoId;

    @Column(nullable = false)
    private String codigoTuss;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private int quantidade;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valorUnitario;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    public void calcularTotal() {
        this.valorTotal = valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
