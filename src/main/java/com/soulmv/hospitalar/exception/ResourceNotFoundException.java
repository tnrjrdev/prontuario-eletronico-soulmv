package com.soulmv.hospitalar.exception;

/**
 * Lançada quando um recurso solicitado não é encontrado. Resulta em HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super("%s não encontrado(a) para o identificador: %s".formatted(recurso, id));
    }
}
