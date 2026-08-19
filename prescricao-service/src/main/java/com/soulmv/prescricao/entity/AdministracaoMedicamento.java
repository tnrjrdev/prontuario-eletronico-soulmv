package com.soulmv.prescricao.entity;

import com.soulmv.prescricao.enums.StatusAdministracao;
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

@Entity
@Table(name = "administracoes_medicamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministracaoMedicamento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_prescricao_id", nullable = false)
    private ItemPrescricao itemPrescricao;

    @Column(name = "enfermeiro_id", nullable = false)
    private Long enfermeiroId;

    private String enfermeiroNome;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusAdministracao status;

    @Column(nullable = false)
    private LocalDateTime dataHoraAdministracao;

    private String observacao;
}
