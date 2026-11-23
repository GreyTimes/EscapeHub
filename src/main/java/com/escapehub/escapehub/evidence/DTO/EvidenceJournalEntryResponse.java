package com.escapehub.escapehub.evidence.dto;

import java.time.Instant;
import java.util.UUID;

public record EvidenceJournalEntryResponse(
        UUID discoveryId,
        UUID evidenceId,
        String title,
        String description,
        UUID roomId,
        UUID discoveredByUserId,
        Instant discoveredAt
) {
}