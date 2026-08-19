package com.soulmv.prescricao.entity;

import com.soulmv.prescricao.enums.StatusPrescricao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Prescrição médica de um atendimento (dono: atendimento-service, validado via Feign).
 * Itens/administrações vivem neste mesmo serviço, então mantêm relação JPA local.
 */
@Entity
@Table(name = "prescricoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescricao extends BaseEntity {

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    private String pacienteNome;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    private String medicoNome;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusPrescricao status = StatusPrescricao.ATIVA;

    private String observacao;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @OneToMany(mappedBy = "prescricao", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPrescricao> itens = new ArrayList<>();

    public void addItem(ItemPrescricao item) {
        item.setPrescricao(this);
        this.itens.add(item);
    }
}
