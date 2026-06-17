package com.soulmv.paciente.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de exceÃ§Ãµes no padrÃ£o RFC 7807 (ProblemDetail).
 * Centraliza as respostas de erro sem vazar stack trace ao cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Recurso nÃ£o encontrado", ex.getMessage(), "not-found");
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        return build(ex.getStatus(), "Regra de negÃ³cio violada", ex.getMessage(), "business-rule");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = build(HttpStatus.BAD_REQUEST, "Falha de validaÃ§Ã£o",
                "Um ou mais campos sÃ£o invÃ¡lidos.", "validation");
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            erros.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty("erros", erros);
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraint(ConstraintViolationException ex) {
        return build(HttpStatus.BAD_REQUEST, "Falha de validaÃ§Ã£o", ex.getMessage(), "validation");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "Conflito de integridade",
                "A operaÃ§Ã£o viola uma restriÃ§Ã£o de integridade dos dados (ex.: valor duplicado).",
                "data-integrity");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado",
                "VocÃª nÃ£o possui permissÃ£o para acessar este recurso.", "forbidden");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "NÃ£o autenticado",
                "Credenciais ausentes ou invÃ¡lidas.", "unauthorized");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "RequisiÃ§Ã£o invÃ¡lida", ex.getMessage(), "bad-request");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "MÃ©todo nÃ£o suportado",
                "O mÃ©todo " + ex.getMethod() + " nÃ£o Ã© permitido neste recurso.", "method-not-allowed");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Corpo da requisiÃ§Ã£o invÃ¡lido",
                "NÃ£o foi possÃ­vel ler o corpo da requisiÃ§Ã£o (JSON malformado ou ausente).", "malformed-body");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Contate o suporte.", "internal-error");
    }

    private ProblemDetail build(HttpStatus status, String title, String detail, String tipo) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("https://soulmv.com/problems/" + tipo));
        pd.setProperty("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        return pd;
    }
}
