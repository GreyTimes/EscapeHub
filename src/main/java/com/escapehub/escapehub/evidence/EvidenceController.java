package com.escapehub.escapehub.evidence;

import com.escapehub.escapehub.evidence.dto.DiscoverEvidenceRequest;
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
    public List<EvidenceJournalEntryResponse> getEvidenceJournal(@PathVariable UUID sessionId) {
        return evidenceService.getEvidenceJournal(sessionId);
    }

    @PostMapping("/{evidenceId}/discover")
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceJournalEntryResponse discoverEvidence(
            @PathVariable UUID sessionId,
            @PathVariable UUID evidenceId,
            @RequestBody DiscoverEvidenceRequest request
    ) {
        return evidenceService.discoverEvidence(sessionId, evidenceId, request);
    }
}