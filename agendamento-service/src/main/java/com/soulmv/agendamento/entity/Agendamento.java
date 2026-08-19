package com.soulmv.agendamento.entity;

import com.soulmv.agendamento.enums.StatusAgendamento;
import com.soulmv.agendamento.enums.TipoAgendamento;
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

    /**
     * Paciente/profissional/setor/convênio são donos de outros microsserviços
     * (paciente-service, iam-service, catalogo-service) — não são mais relações JPA
     * locais. O nome é um retrato de quando o agendamento foi criado/reagendado
     * (validado via Feign nesse momento); se o cadastro de origem mudar depois, a
     * agenda já marcada não muda retroativamente — comportamento aceitável (e até
     * desejável) para uma tela de agenda, e evita 1 chamada HTTP por linha ao listar.
     */
    @Column(nullable = false)
    private Long pacienteId;

    @Column(nullable = false)
    private String pacienteNome;

    /** Profissional (corpo clínico) que atenderá o compromisso. */
    @Column(nullable = false)
    private Long profissionalId;

    @Column(nullable = false)
    private String profissionalNome;

    @Column(nullable = false)
    private Long setorId;

    @Column(nullable = false)
    private String setorNome;

    private Long convenioId;

    private String convenioNome;

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
