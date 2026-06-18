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
 * Código da CID-10 (Classificação Internacional de Doenças), usado nos
 * diagnósticos (Etapa 7).
 */
@Entity
@Table(name = "cid10")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cid10 extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String descricao;
}
