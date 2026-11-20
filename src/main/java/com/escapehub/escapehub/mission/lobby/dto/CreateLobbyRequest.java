package com.escapehub.escapehub.mission.lobby.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateLobbyRequest(
        @NotNull(message = "missionVersionId is required")
        UUID missionVersionId
) {
}