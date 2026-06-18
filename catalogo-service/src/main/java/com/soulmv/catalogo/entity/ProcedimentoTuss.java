package com.soulmv.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Procedimento da tabela TUSS (Terminologia Unificada da Saúde Suplementar),
 * usado nos itens de conta e nas guias TISS (Etapa 8).
 */
@Entity
@Table(name = "procedimentos_tuss")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedimentoTuss extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String codigoTuss;

    @Column(nullable = false)
    private String descricao;

    /** Valor de referência para cobrança. */
    @Column(precision = 12, scale = 2)
    private BigDecimal valorReferencia;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
