package com.escapehub.escapehub.evidence.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.evidence.dto.AvailableEvidenceResponse;
import com.escapehub.escapehub.evidence.dto.EvidenceJournalEntryResponse;
import com.escapehub.escapehub.evidence.service.EvidenceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @GetMapping("/evidence")
    public List<EvidenceJournalEntryResponse> getEvidenceJournal(
            @PathVariable UUID sessionId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId
    ) {
        return evidenceService.getEvidenceJournal(sessionId, currentUserId);
    }

    @GetMapping("/nodes/{nodeId}/evidence")
    public List<AvailableEvidenceResponse> getAvailableEvidence(
            @PathVariable UUID sessionId,
            @PathVariable UUID nodeId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId
    ) {
        return evidenceService.getAvailableEvidence(
                sessionId,
                nodeId,
                currentUserId
        );
    }

    @PostMapping("/evidence/{evidenceId}/discover")
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceJournalEntryResponse discoverEvidence(
            @PathVariable UUID sessionId,
            @PathVariable UUID evidenceId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId
    ) {
        return evidenceService.discoverEvidence(
                sessionId,
                evidenceId,
                currentUserId
        );
    }
}