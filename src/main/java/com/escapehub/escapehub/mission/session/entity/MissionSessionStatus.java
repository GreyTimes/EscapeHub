package com.escapehub.escapehub.mission.session.entity;

/**
 * Lifecycle of a mission session (architecture section 6.2).
 * There is no FAILED state.
 */
public enum MissionSessionStatus {
    ACTIVE,
    COMPLETED,
    ABANDONED
}
