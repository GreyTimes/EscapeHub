package com.escapehub.escapehub.evidence;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.evidence.dto.EvidenceJournalEntryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @GetMapping
    public List<EvidenceJournalEntryResponse> getEvidenceJournal(
            @PathVariable UUID sessionId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId
    ) {
        return evidenceService.getEvidenceJournal(sessionId, currentUserId);
    }

    @PostMapping("/{evidenceId}/discover")
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceJournalEntryResponse discoverEvidence(
            @PathVariable UUID sessionId,
            @PathVariable UUID evidenceId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId
    ) {
        return evidenceService.discoverEvidence(sessionId, evidenceId, currentUserId);
    }
}
