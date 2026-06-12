package com.soulmv.hospitalar.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
 * Resultado/laudo de uma solicitação de exame (1:1). O laudo em PDF, quando
 * houver, é referenciado por {@link Anexo}.
 */
@Entity
@Table(name = "resultados_exame")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoExame extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false, unique = true)
    private SolicitacaoExame solicitacao;

    @Lob
    private String resultadoTexto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laudo_anexo_id")
    private Anexo laudo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liberado_por_id", nullable = false)
    private Usuario liberadoPor;

    private LocalDateTime dataLiberacao;
}
