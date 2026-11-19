package com.escapehub.escapehub.mission.session.dto;

import com.escapehub.escapehub.mission.session.entity.SessionMode;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Body of POST /api/v1/missions/{missionId}/sessions.
 *
 * mode: the play mode chosen by the player (SOLO or TEAM).
 * missionVersionId: the published version of the mission to play.
 */
public record StartSessionRequest(
        @NotNull(message = "mode is required (SOLO or TEAM)")
        SessionMode mode,

        @NotNull(message = "missionVersionId is required")
        UUID missionVersionId
) {
}
