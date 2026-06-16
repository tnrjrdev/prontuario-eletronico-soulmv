package com.soulmv.hospitalar.entity;

import com.soulmv.hospitalar.enums.StatusAgendamento;
import com.soulmv.hospitalar.enums.TipoAgendamento;
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
 * Agendamento (marcação prévia de consulta/exame/procedimento). A recepção cria
 * o compromisso; no comparecimento, o check-in o converte em um {@link Atendimento}.
 */
@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    /** Profissional (corpo clínico) que atenderá o compromisso. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Usuario profissional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_id")
    private Convenio convenio;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoAgendamento tipo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    @Builder.Default
    private Integer duracaoMinutos = 30;

    @Column(length = 1000)
    private String observacoes;

    /** Atendimento gerado no check-in (preenchido quando status = REALIZADO). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id")
    private Atendimento atendimento;

    /** Fim previsto do compromisso (para checagem de conflito de horário). */
    public LocalDateTime getFimPrevisto() {
        int dur = duracaoMinutos != null ? duracaoMinutos : 30;
        return dataHora.plusMinutes(dur);
    }
}
