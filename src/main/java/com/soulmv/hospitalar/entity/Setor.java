package com.soulmv.hospitalar.entity;

import com.soulmv.hospitalar.enums.TipoSetor;
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

/**
 * Setor ou unidade do hospital (ex.: Emergência, UTI, Ambulatório).
 */
@Entity
@Table(name = "setores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setor extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoSetor tipo;

    private String descricao;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
