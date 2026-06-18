package com.soulmv.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item do catálogo de medicamentos, usado nas prescrições (Etapa 7).
 */
@Entity
@Table(name = "medicamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicamento extends BaseEntity {

    @Column(nullable = false)
    private String nome;

    private String principioAtivo;

    /** Ex.: "500mg", "10mg/mL". */
    private String concentracao;

    /** Medicamento controlado/restrito (psicotrópicos, antibióticos controlados). */
    @Column(nullable = false)
    @Builder.Default
    private boolean controlado = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
