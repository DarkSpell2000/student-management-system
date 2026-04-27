package com.studentmanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Централизованная обработка исключений.
 * Возвращает JSON с описанием ошибки вместо стандартных HTML страниц Spring.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Ошибки валидации @Valid — 400 Bad Request */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            fieldErrors.put(field, error.getDefaultMessage());
        });
        return buildError(HttpStatus.BAD_REQUEST, "Ошибка валидации", fieldErrors);
    }

    /** Неверный пароль — 401 */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль", null);
    }

    /** Нет прав — 403 */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    /** Не найдено — 404 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(IllegalArgumentException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /** Бизнес-ошибки (нельзя удалить, дублирование) — 409 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /** Всё остальное — 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        log.error("Unexpected error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
            "Внутренняя ошибка сервера", null);
    }

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (details != null) body.put("details", details);
        return ResponseEntity.status(status).body(body);
    }
}
