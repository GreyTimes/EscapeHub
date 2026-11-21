package com.escapehub.escapehub.mission.map.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MovePlayerRequest(
        @NotNull(message = "targetNodeId is required")
        UUID targetNodeId
) {
}