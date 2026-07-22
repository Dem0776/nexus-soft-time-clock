package com.condor.nexussoft.timeclock.identity.infrastructure.web;

import com.condor.nexussoft.timeclock.identity.domain.exception.AccountLockedException;
import com.condor.nexussoft.timeclock.identity.domain.exception.InvalidCredentialsException;
import com.condor.nexussoft.timeclock.identity.domain.exception.InvalidRefreshTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Traduce las excepciones de autenticación a códigos HTTP semánticos
 * (401 credenciales/refresh inválido, 423 cuenta bloqueada), con cuerpo ProblemDetail.
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
    public ProblemDetail handleUnauthorized(RuntimeException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage(), codeOf(ex));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ProblemDetail handleLocked(AccountLockedException ex) {
        return problem(HttpStatus.LOCKED, ex.getMessage(), ex.getCode());
    }

    private ProblemDetail problem(HttpStatus status, String detail, String code) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle("Autenticación");
        pd.setProperty("code", code);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    private String codeOf(RuntimeException ex) {
        return ex instanceof com.condor.nexussoft.timeclock.shared.domain.DomainException de
                ? de.getCode() : "UNAUTHORIZED";
    }
}
