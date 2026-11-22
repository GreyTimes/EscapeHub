package com.escapehub.escapehub.mission.definition.dto;
import java.util.UUID;

public record MissionSummaryResponse(
        UUID missionId,
        UUID missionVersionId,
        String title,
        String description
) {
}
