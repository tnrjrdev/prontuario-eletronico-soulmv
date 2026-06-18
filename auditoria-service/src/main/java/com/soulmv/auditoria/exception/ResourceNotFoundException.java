package com.soulmv.auditoria.exception;

/**
 * LanÃ§ada quando um recurso solicitado nÃ£o Ã© encontrado. Resulta em HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super("%s nÃ£o encontrado(a) para o identificador: %s".formatted(recurso, id));
    }
}
