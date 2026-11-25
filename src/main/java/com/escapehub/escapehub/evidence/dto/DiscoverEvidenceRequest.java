package com.escapehub.escapehub.evidence.dto;

import java.util.UUID;

public record DiscoverEvidenceRequest(
        UUID discoveredByUserId,
        UUID currentRoomId
) {
}