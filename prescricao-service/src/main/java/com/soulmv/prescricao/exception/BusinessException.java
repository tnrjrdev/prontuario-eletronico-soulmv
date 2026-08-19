package com.soulmv.prescricao.exception;

import org.springframework.http.HttpStatus;

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
