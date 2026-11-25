package com.escapehub.escapehub.evidence.service;

import com.escapehub.escapehub.evidence.dto.EvidenceJournalEntryResponse;
import com.escapehub.escapehub.evidence.entity.Evidence;
import com.escapehub.escapehub.evidence.repository.EvidenceRepository;
import com.escapehub.escapehub.evidence.session.SessionEvidenceDiscovery;
import com.escapehub.escapehub.evidence.session.SessionEvidenceDiscoveryRepository;
import com.escapehub.escapehub.mission.map.entity.MissionNode;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.ParticipationStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
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
    private final MissionSessionRepository sessionRepository;
    private final SessionMemberRepository memberRepository;
    private final MissionNodeRepository nodeRepository;

    public EvidenceService(
            EvidenceRepository evidenceRepository,
            SessionEvidenceDiscoveryRepository discoveryRepository,
            MissionSessionRepository sessionRepository,
            SessionMemberRepository memberRepository,
            MissionNodeRepository nodeRepository
    ) {
        this.evidenceRepository = evidenceRepository;
        this.discoveryRepository = discoveryRepository;
        this.sessionRepository = sessionRepository;
        this.memberRepository = memberRepository;
        this.nodeRepository = nodeRepository;
    }

    @Transactional
    public EvidenceJournalEntryResponse discoverEvidence(
            UUID sessionId,
            UUID evidenceId,
            UUID currentUserId
    ) {
        requireIdentifiers(sessionId, evidenceId, currentUserId);

        MissionSession session = findSession(sessionId);
        if (!session.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The mission session is not active.");
        }

        SessionMember member = findMember(sessionId, currentUserId);
        if (member.getParticipationStatus() != ParticipationStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not an active member of this session.");
        }

        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidence not found."));

        MissionNode node = nodeRepository.findById(evidence.getNodeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidence room not found."));

        if (!node.getMissionVersionId().equals(session.getMissionVersionId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Evidence does not belong to this mission session."
            );
        }

        if (member.getCurrentNodeId() == null || !member.getCurrentNodeId().equals(node.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Player is not currently in the room containing this evidence."
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
                currentUserId,
                Instant.now()
        );

        try {
            SessionEvidenceDiscovery saved = discoveryRepository.saveAndFlush(discovery);
            return toResponse(saved, evidence);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Evidence has already been discovered in this session."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<EvidenceJournalEntryResponse> getEvidenceJournal(UUID sessionId, UUID currentUserId) {
        if (sessionId == null || currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A current session user is required.");
        }

        findSession(sessionId);
        findMember(sessionId, currentUserId);

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
                .map(discovery -> toResponse(discovery, evidenceById.get(discovery.getEvidenceId())))
                .toList();
    }

    private MissionSession findSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission session not found."));
    }

    private SessionMember findMember(UUID sessionId, UUID currentUserId) {
        return memberRepository.findBySessionIdAndUserId(sessionId, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not a member of this mission session."
                ));
    }

    private void requireIdentifiers(UUID sessionId, UUID evidenceId, UUID currentUserId) {
        if (sessionId == null || evidenceId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session ID and Evidence ID are required.");
        }
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A current session user is required.");
        }
    }

    private EvidenceJournalEntryResponse toResponse(
            SessionEvidenceDiscovery discovery,
            Evidence evidence
    ) {
        String title = evidence != null ? evidence.getTitle() : "Unknown Evidence";
        String description = evidence != null && evidence.getDescription() != null
                ? evidence.getDescription()
                : "";
        UUID nodeId = evidence != null ? evidence.getNodeId() : null;

        return new EvidenceJournalEntryResponse(
                discovery.getId(),
                discovery.getEvidenceId(),
                title,
                description,
                nodeId,
                discovery.getDiscoveredByUserId(),
                discovery.getDiscoveredAt()
        );
    }
}
