package com.soulmv.hospitalar.entity;

import com.soulmv.hospitalar.enums.StatusExame;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Solicitação de exame de um atendimento, feita pelo médico.
 */
@Entity
@Table(name = "solicitacoes_exame")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoExame extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_solicitante_id", nullable = false)
    private Usuario medicoSolicitante;

    @Column(nullable = false)
    private String tipoExame;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusExame status = StatusExame.SOLICITADO;

    private String observacao;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;

    @OneToOne(mappedBy = "solicitacao", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private ResultadoExame resultado;
}
