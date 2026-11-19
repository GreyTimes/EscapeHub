package com.escapehub.escapehub.mission.session.dto;

import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMode;

import java.time.Instant;
import java.util.UUID;

/**
 * What the API returns for a mission session.
 * JPA entities are never returned directly.
 */
public record SessionResponse(
        UUID id,
        UUID missionVersionId,
        SessionMode mode,
        MissionSessionStatus status,
        UUID teamId,
        UUID startedByUserId,
        Instant startedAt
) {

    public static SessionResponse from(MissionSession session) {
        return new SessionResponse(
                session.getId(),
                session.getMissionVersionId(),
                session.getMode(),
                session.getStatus(),
                session.getTeamId(),
                session.getStartedByUserId(),
                session.getStartedAt()
        );
    }
}
