package com.sih.tenant.config;

import com.sih.tenant.dto.Response;
import com.sih.tenant.exception.QuotaExceededException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String SERVICE_NAME = "TENANT-SERVICE";

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Response<Object>> handleNotFound(EntityNotFoundException ex) {
        Response<Object> body = Response.builder()
                .status(HttpStatus.NOT_FOUND)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .service(SERVICE_NAME)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<Response<Object>> handleQuota(QuotaExceededException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("operation", ex.getOperation());
        details.put("period", ex.getPeriod());
        details.put("limit", ex.getLimit());
        details.put("current", ex.getCurrent());
        details.put("code", "QUOTA_EXCEEDED");
        Response<Object> body = Response.builder()
                .status(HttpStatus.FORBIDDEN)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage())
                .service(SERVICE_NAME)
                .data(details)
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        Response<Object> body = Response.builder()
                .status(HttpStatus.BAD_REQUEST)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("validation.error")
                .service(SERVICE_NAME)
                .data(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Response<Object> body = Response.builder()
                .status(HttpStatus.BAD_REQUEST)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .service(SERVICE_NAME)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handleGeneric(Exception ex) {
        log.error("Erreur inattendue : {}", ex.getMessage(), ex);
        Response<Object> body = Response.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("internal.server.error")
                .service(SERVICE_NAME)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
