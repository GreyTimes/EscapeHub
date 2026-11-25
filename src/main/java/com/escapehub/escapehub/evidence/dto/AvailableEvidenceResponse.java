package com.escapehub.escapehub.evidence.dto;

import java.util.UUID;

public record AvailableEvidenceResponse(
        UUID id,
        String title,
        String description,
        UUID nodeId
) {
}