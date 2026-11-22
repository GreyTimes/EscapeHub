package com.escapehub.escapehub.mission.session.service;

public class SessionException extends RuntimeException {
    public enum Reason {
        MISSING_CURRENT_USER,
        ACTIVE_SESSION_EXISTS,
        TEAM_MODE_REQUIRES_LOBBY,
        SESSION_NOT_FOUND,
        NOT_A_SESSION_MEMBER,
        SESSION_NOT_ACTIVE,
        FORBIDDEN_ACTION,
        TEAM_NOT_FOUND,
        LOBBY_NOT_FOUND,
        LOBBY_NOT_WAITING,
        ACTIVE_LOBBY_EXISTS,
        ALREADY_IN_LOBBY,
        LOBBY_FULL,
        NOT_ENOUGH_PARTICIPANTS,
        NODE_NOT_FOUND,
        NODE_NOT_CONNECTED,
        PUZZLE_NOT_FOUND,
        NOT_IN_PUZZLE_ROOM
    }

    private final Reason reason;

    public SessionException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
