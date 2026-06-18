package com.soulmv.faturamento.entity;

import com.soulmv.faturamento.enums.StatusConta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conta hospitalar de um atendimento (1:1), com itens de procedimento (TUSS).
 */
@Entity
@Table(name = "contas_hospitalares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaHospitalar extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false, unique = true)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_id")
    private Convenio convenio;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusConta status = StatusConta.ABERTA;

    @Column(precision = 14, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    private LocalDateTime dataFechamento;

    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemConta> itens = new ArrayList<>();

    public void addItem(ItemConta item) {
        item.setConta(this);
        this.itens.add(item);
        recalcularTotal();
    }

    public void recalcularTotal() {
        this.valorTotal = itens.stream()
                .map(ItemConta::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
