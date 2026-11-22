package com.escapehub.escapehub.mission.session.dto;

import com.escapehub.escapehub.mission.session.entity.SessionMode;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartSessionRequest(
        @NotNull(message = "mode is required (SOLO or TEAM)")
        SessionMode mode,

        @NotNull(message = "missionVersionId is required")
        UUID missionVersionId
) {
}
