package com.soulmv.agendamento.entity;

import com.soulmv.agendamento.enums.TipoConvenio;
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
 * Convênio / plano de saúde (ou forma de pagamento particular/SUS).
 */
@Entity
@Table(name = "convenios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Convenio extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String nome;

    /** Registro na ANS (quando plano de saúde). */
    private String registroAns;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoConvenio tipo;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
