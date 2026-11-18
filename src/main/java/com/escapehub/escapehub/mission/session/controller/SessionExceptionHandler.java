package com.escapehub.escapehub.mission.session.controller;

import com.escapehub.escapehub.mission.session.service.SessionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * Turns session errors into JSON responses of the form {"message": "..."},
 * which frontend/src/services/api.js already knows how to read.
 *
 * Limited to this module so it does not affect other controllers.
 */
@RestControllerAdvice(basePackageClasses = MissionSessionController.class)
public class SessionExceptionHandler {

    @ExceptionHandler(SessionException.class)
    public ResponseEntity<Map<String, String>> handleSessionException(SessionException exception) {
        HttpStatus status = switch (exception.getReason()) {
            case MISSING_CURRENT_USER -> HttpStatus.UNAUTHORIZED;
            case TEAM_MODE_REQUIRES_LOBBY -> HttpStatus.BAD_REQUEST;
            case ACTIVE_SESSION_EXISTS -> HttpStatus.CONFLICT;
            case SESSION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case NOT_A_SESSION_MEMBER -> HttpStatus.FORBIDDEN;
        };

        return error(status, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("Invalid request.");

        return error(HttpStatus.BAD_REQUEST, message);
    }

    // Missing body, broken JSON or a mode other than SOLO / TEAM.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid request body. mode must be SOLO or TEAM.");
    }

    // An id in the URL that is not a valid UUID.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getName() + " must be a valid UUID.");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
