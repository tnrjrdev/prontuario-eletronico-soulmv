package com.soulmv.agendamento.entity;

import com.soulmv.agendamento.enums.StatusLeito;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Leito pertencente a um setor. O vínculo com o atendimento que o ocupa será
 * adicionado na Etapa 5 (Atendimentos).
 */
@Entity
@Table(name = "leitos", uniqueConstraints =
        @UniqueConstraint(name = "uk_leito_setor_identificador", columnNames = {"setor_id", "identificador"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leito extends BaseEntity {

    /** Identificação do leito dentro do setor (ex.: "UTI-03", "201-A"). */
    @Column(nullable = false)
    private String identificador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusLeito status = StatusLeito.LIVRE;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
