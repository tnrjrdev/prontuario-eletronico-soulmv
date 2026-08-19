package com.soulmv.exames.entity;

import com.soulmv.exames.enums.StatusExame;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Solicitação de exame de um atendimento (dono: atendimento-service, validado via Feign).
 * O resultado vive neste mesmo serviço, então mantém relação JPA local.
 */
@Entity
@Table(name = "solicitacoes_exame")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoExame extends BaseEntity {

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    private String pacienteNome;

    @Column(name = "medico_solicitante_id", nullable = false)
    private Long medicoSolicitanteId;

    private String medicoSolicitanteNome;

    @Column(nullable = false)
    private String tipoExame;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusExame status = StatusExame.SOLICITADO;

    private String observacao;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;

    @OneToOne(mappedBy = "solicitacao", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ResultadoExame resultado;
}
