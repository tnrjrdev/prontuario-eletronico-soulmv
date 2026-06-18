package com.soulmv.faturamento.exception;

import org.springframework.http.HttpStatus;

/**
 * ViolaÃ§Ã£o de regra de negÃ³cio. Resulta, por padrÃ£o, em HTTP 400 (Bad Request),
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
