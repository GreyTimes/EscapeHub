package com.escapehub.escapehub.evidence;

import com.escapehub.escapehub.evidence.dto.DiscoverEvidenceRequest;
import com.escapehub.escapehub.evidence.dto.EvidenceJournalEntryResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final SessionEvidenceDiscoveryRepository discoveryRepository;

    public EvidenceService(
            EvidenceRepository evidenceRepository,
            SessionEvidenceDiscoveryRepository discoveryRepository
    ) {
        this.evidenceRepository = evidenceRepository;
        this.discoveryRepository = discoveryRepository;
    }

    @Transactional
    public EvidenceJournalEntryResponse discoverEvidence(
            UUID sessionId,
            UUID evidenceId,
            DiscoverEvidenceRequest request
    ) {
        if (sessionId == null || evidenceId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session ID and Evidence ID are required.");
        }
        if (request == null || request.discoveredByUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "discoveredByUserId is required.");
        }

        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidence not found."));

        UUID evidenceRoomId = extractRoomId(evidence);
        if (request.currentRoomId() == null || !request.currentRoomId().equals(evidenceRoomId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Player is not in the same room as the evidence."
            );
        }

        if (discoveryRepository.existsBySessionIdAndEvidenceId(sessionId, evidenceId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Evidence has already been discovered in this session."
            );
        }

        SessionEvidenceDiscovery discovery = new SessionEvidenceDiscovery(
                sessionId,
                evidenceId,
                request.discoveredByUserId(),
                Instant.now()
        );

        try {
            SessionEvidenceDiscovery saved = discoveryRepository.saveAndFlush(discovery);
            return toResponse(saved, evidence);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Evidence has already been discovered in this session."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<EvidenceJournalEntryResponse> getEvidenceJournal(UUID sessionId) {
        List<SessionEvidenceDiscovery> discoveries =
                discoveryRepository.findAllBySessionIdOrderByDiscoveredAtAsc(sessionId);

        if (discoveries.isEmpty()) {
            return List.of();
        }

        List<UUID> evidenceIds = discoveries.stream()
                .map(SessionEvidenceDiscovery::getEvidenceId)
                .distinct()
                .toList();

        Map<UUID, Evidence> evidenceById = evidenceRepository.findAllById(evidenceIds).stream()
                .collect(Collectors.toMap(Evidence::getId, Function.identity()));

        return discoveries.stream()
                .map(disc -> toResponse(disc, evidenceById.get(disc.getEvidenceId())))
                .toList();
    }

    private EvidenceJournalEntryResponse toResponse(SessionEvidenceDiscovery discovery, Evidence evidence) {
        String title = evidence != null ? extractTitle(evidence) : "Unknown Evidence";
        String description = evidence != null ? extractDescription(evidence) : "";
        UUID roomId = evidence != null ? extractRoomId(evidence) : null;

        return new EvidenceJournalEntryResponse(
                discovery.getId(),
                discovery.getEvidenceId(),
                title,
                description,
                roomId,
                discovery.getDiscoveredByUserId(),
                discovery.getDiscoveredAt()
        );
    }

    private UUID extractRoomId(Evidence evidence) {
        return evidence.getRoomId();
    }

    private String extractTitle(Evidence evidence) {
        return evidence.getTitle();
    }

    private String extractDescription(Evidence evidence) {
        return evidence.getDescription();
    }
}