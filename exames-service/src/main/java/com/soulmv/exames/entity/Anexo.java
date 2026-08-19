package com.soulmv.exames.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "anexos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anexo extends BaseEntity {

    @Column(nullable = false)
    private String nomeOriginal;

    @Column(nullable = false)
    private String nomeArmazenado;

    private String contentType;

    private long tamanho;

    @Column(name = "enviado_por_id")
    private Long enviadoPorId;

    private String enviadoPorNome;
}
