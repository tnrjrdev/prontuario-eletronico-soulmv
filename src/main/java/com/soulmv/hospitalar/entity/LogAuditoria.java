package com.soulmv.hospitalar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro imutável (append-only) da trilha de auditoria. Não estende
 * BaseEntity para deixar explícito que não há atualização — apenas inserção.
 */
@Entity
@Table(name = "logs_auditoria")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Login do usuário autenticado (ou "anonimo"). */
    @Column(nullable = false)
    private String usuarioLogin;

    /** Método HTTP (GET, POST, ...). */
    @Column(nullable = false)
    private String metodo;

    /** Caminho acessado (URI). */
    @Column(nullable = false, length = 500)
    private String caminho;

    /** Código de status HTTP da resposta. */
    private int status;

    /** Endereço de origem. */
    private String ip;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
