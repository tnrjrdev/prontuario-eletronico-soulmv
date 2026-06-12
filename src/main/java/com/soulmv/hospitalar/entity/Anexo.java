package com.soulmv.hospitalar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metadados de um arquivo armazenado (laudos em PDF, etc.). O binário fica no
 * filesystem; aqui guardamos apenas o caminho/nome e metadados.
 */
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

    /** Nome único do arquivo gravado no storage. */
    @Column(nullable = false)
    private String nomeArmazenado;

    private String contentType;

    private long tamanho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enviado_por_id")
    private Usuario enviadoPor;
}
