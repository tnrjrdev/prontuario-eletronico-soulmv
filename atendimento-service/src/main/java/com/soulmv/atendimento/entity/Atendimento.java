package com.soulmv.atendimento.entity;

import com.soulmv.atendimento.enums.StatusAtendimento;
import com.soulmv.atendimento.enums.TipoAtendimento;
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

import java.time.LocalDateTime;

/**
 * Atendimento (encontro) — abertura do paciente no hospital e o eixo ao qual se
 * vinculam triagem, evoluções, prescrições, exames e conta.
 *
 * <p>Paciente, setor, leito e profissional responsável são donos de outros
 * microsserviços (paciente-service, catalogo-service, iam-service) — sem relação JPA
 * local, só id + nome denormalizado (mesmo raciocínio do agendamento-service: evita
 * N+1 Feign ao listar a fila de atendimentos, que é a operação mais comum aqui).</p>
 *
 * <p>Esta tabela ({@code atendimentos}) ainda é lida via JPA direto pelo monólito
 * (triagem, anamnese, diagnóstico, prescrição, administração, exames e evolução —
 * nenhum desses 8 domínios foi extraído ainda). O schema físico das colunas
 * paciente_id/setor_id/leito_id/profissional_responsavel_id não mudou — só a forma
 * como este serviço as mapeia (Long em vez de @ManyToOne).</p>
 */
@Entity
@Table(name = "atendimentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Atendimento extends BaseEntity {

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    private String pacienteNome;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoAtendimento tipo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusAtendimento status = StatusAtendimento.AGUARDANDO_TRIAGEM;

    @Column(name = "setor_id", nullable = false)
    private Long setorId;

    private String setorNome;

    @Column(name = "leito_id")
    private Long leitoId;

    private String leitoIdentificador;

    @Column(name = "profissional_responsavel_id")
    private Long profissionalResponsavelId;

    private String profissionalResponsavelNome;

    @Column(length = 1000)
    private String queixaPrincipal;

    @Column(nullable = false)
    private LocalDateTime dataEntrada;

    private LocalDateTime dataAlta;
}
