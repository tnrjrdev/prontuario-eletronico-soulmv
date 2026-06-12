package com.soulmv.hospitalar.entity;

import com.soulmv.hospitalar.enums.StatusGuiaTiss;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Guia TISS gerada a partir de uma conta hospitalar para envio ao convênio.
 * O XML gerado fica armazenado em {@code xml}.
 */
@Entity
@Table(name = "guias_tiss")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaTiss extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private ContaHospitalar conta;

    @Column(nullable = false, unique = true)
    private String numeroGuia;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusGuiaTiss status = StatusGuiaTiss.GERADA;

    @Lob
    @Column(nullable = false)
    private String xml;

    @Column(nullable = false)
    private LocalDateTime dataGeracao;
}
