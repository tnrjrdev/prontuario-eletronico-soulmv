package com.soulmv.hospitalar.exception;

import org.springframework.http.HttpStatus;

/**
 * Violação de regra de negócio. Resulta, por padrão, em HTTP 400 (Bad Request),
 * mas permite informar outro status (ex.: 409 Conflict para duplicidades).
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String mensagem) {
        this(mensagem, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
