package com.escapehub.escapehub.mission.session.service;

/**
 * Business-rule errors of the mission session module.
 * The controller layer maps each Reason to an HTTP status.
 */
public class SessionException extends RuntimeException {

    public enum Reason {
        MISSING_CURRENT_USER,
        ACTIVE_SESSION_EXISTS,
        TEAM_MODE_REQUIRES_LOBBY,
        SESSION_NOT_FOUND,
        NOT_A_SESSION_MEMBER
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
