
package com.escapehub.escapehub.mission.map.dto;

import java.util.List;
import java.util.UUID;

public record MapResponse(
        UUID sessionId,
        UUID missionVersionId,
        UUID currentNodeId,
        List<MapNodeResponse> nodes,
        List<Connection> connections,
        List<MapPlayerResponse> players
) {
    public MapResponse(
            UUID sessionId,
            UUID missionVersionId,
            UUID currentNodeId,
            List<MapNodeResponse> nodes,
            List<Connection> connections
    ) {
        this(
                sessionId,
                missionVersionId,
                currentNodeId,
                nodes,
                connections,
                List.of()
        );
    }

    public record Connection(UUID sourceNodeId, UUID targetNodeId) {
    }
}
