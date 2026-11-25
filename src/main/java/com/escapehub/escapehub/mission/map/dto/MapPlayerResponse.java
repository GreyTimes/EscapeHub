
package com.escapehub.escapehub.mission.map.dto;

import java.util.UUID;

public record MapPlayerResponse(
        UUID userId,
        String displayName,
        UUID currentNodeId,
        boolean currentPlayer
) {
}
