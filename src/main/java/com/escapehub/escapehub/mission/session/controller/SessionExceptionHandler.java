package com.escapehub.escapehub.mission.session.controller;

import com.escapehub.escapehub.mission.session.service.SessionException;
import com.escapehub.escapehub.mission.lobby.controller.LobbyController;
import com.escapehub.escapehub.mission.map.controller.MissionMapController;
import com.escapehub.escapehub.puzzle.controller.PuzzleController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice(basePackageClasses = {MissionSessionController.class,LobbyController.class,MissionMapController.class,PuzzleController.class})
public class SessionExceptionHandler {
    @ExceptionHandler(SessionException.class)
    public ResponseEntity<Map<String, String>> handleSessionException(SessionException exception) {
        HttpStatus status = switch (exception.getReason()) {
            case MISSING_CURRENT_USER -> HttpStatus.UNAUTHORIZED;
            case TEAM_MODE_REQUIRES_LOBBY -> HttpStatus.BAD_REQUEST;
            case ACTIVE_SESSION_EXISTS -> HttpStatus.CONFLICT;
            case SESSION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case NOT_A_SESSION_MEMBER -> HttpStatus.FORBIDDEN;
            case SESSION_NOT_ACTIVE -> HttpStatus.CONFLICT;
            case NOT_ENOUGH_PARTICIPANTS -> HttpStatus.BAD_REQUEST;
            case FORBIDDEN_ACTION -> HttpStatus.FORBIDDEN;
            case TEAM_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case LOBBY_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case LOBBY_NOT_WAITING -> HttpStatus.CONFLICT;
            case ACTIVE_LOBBY_EXISTS -> HttpStatus.CONFLICT;
            case ALREADY_IN_LOBBY -> HttpStatus.CONFLICT;
            case LOBBY_FULL -> HttpStatus.CONFLICT;
            case NODE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case NODE_NOT_CONNECTED -> HttpStatus.BAD_REQUEST;
            case PUZZLE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case NOT_IN_PUZZLE_ROOM -> HttpStatus.FORBIDDEN;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid request body.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getName() + " must be a valid UUID.");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
