package com.soulmv.hospitalar.entity;

import com.soulmv.hospitalar.enums.StatusAtendimento;
import com.soulmv.hospitalar.enums.TipoAtendimento;
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

import java.time.LocalDateTime;

/**
 * Atendimento (encontro) — abertura do paciente no hospital e o eixo ao qual se
 * vinculam triagem, evoluções, prescrições, exames e conta (etapas seguintes).
 */
@Entity
@Table(name = "atendimentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Atendimento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoAtendimento tipo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusAtendimento status = StatusAtendimento.AGUARDANDO_TRIAGEM;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leito_id")
    private Leito leito;

    /** Profissional (Usuario com perfil clínico) responsável pelo atendimento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_responsavel_id")
    private Usuario profissionalResponsavel;

    @Column(length = 1000)
    private String queixaPrincipal;

    @Column(nullable = false)
    private LocalDateTime dataEntrada;

    private LocalDateTime dataAlta;
}
