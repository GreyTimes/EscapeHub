package com.escapehub.escapehub.evidence;

import com.escapehub.escapehub.evidence.dto.DiscoverEvidenceRequest;
import com.escapehub.escapehub.evidence.dto.EvidenceJournalEntryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private SessionEvidenceDiscoveryRepository discoveryRepository;

    @InjectMocks
    private EvidenceService evidenceService;

    private UUID sessionId;
    private UUID evidenceId;
    private UUID roomId;
    private UUID userId;
    private Evidence sampleEvidence;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        evidenceId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        userId = UUID.randomUUID();

        sampleEvidence = new Evidence(evidenceId, roomId, "Cipher Note", "A torn note with numbers");
    }

    @Test
    void discoverEvidence_success_recordsDiscoveryAndReturnsEntry() {
        when(evidenceRepository.findById(evidenceId)).thenReturn(Optional.of(sampleEvidence));
        when(discoveryRepository.existsBySessionIdAndEvidenceId(sessionId, evidenceId)).thenReturn(false);
        when(discoveryRepository.saveAndFlush(any(SessionEvidenceDiscovery.class))).thenAnswer(invocation -> {
            SessionEvidenceDiscovery d = invocation.getArgument(0);
            return new SessionEvidenceDiscovery(
                    d.getSessionId(),
                    d.getEvidenceId(),
                    d.getDiscoveredByUserId(),
                    Instant.now()
            );
        });

        DiscoverEvidenceRequest request = new DiscoverEvidenceRequest(userId, roomId);
        EvidenceJournalEntryResponse response = evidenceService.discoverEvidence(sessionId, evidenceId, request);

        assertNotNull(response);
        assertEquals(evidenceId, response.evidenceId());
        assertEquals("Cipher Note", response.title());
        assertEquals(userId, response.discoveredByUserId());
        verify(discoveryRepository, times(1)).saveAndFlush(any(SessionEvidenceDiscovery.class));
    }

    @Test
    void discoverEvidence_duplicateInSameSession_throwsConflict() {
        when(evidenceRepository.findById(evidenceId)).thenReturn(Optional.of(sampleEvidence));
        when(discoveryRepository.existsBySessionIdAndEvidenceId(sessionId, evidenceId)).thenReturn(true);

        DiscoverEvidenceRequest request = new DiscoverEvidenceRequest(userId, roomId);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> evidenceService.discoverEvidence(sessionId, evidenceId, request)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(discoveryRepository, never()).saveAndFlush(any());
    }

    @Test
    void discoverEvidence_wrongRoom_throwsConflict() {
        UUID otherRoomId = UUID.randomUUID();
        when(evidenceRepository.findById(evidenceId)).thenReturn(Optional.of(sampleEvidence));

        DiscoverEvidenceRequest request = new DiscoverEvidenceRequest(userId, otherRoomId);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> evidenceService.discoverEvidence(sessionId, evidenceId, request)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(discoveryRepository, never()).saveAndFlush(any());
    }

    @Test
    void getEvidenceJournal_returnsSessionEntries() {
        SessionEvidenceDiscovery disc = new SessionEvidenceDiscovery(
                sessionId, evidenceId, userId, Instant.parse("2026-11-23T10:00:00Z")
        );
        when(discoveryRepository.findAllBySessionIdOrderByDiscoveredAtAsc(sessionId)).thenReturn(List.of(disc));
        when(evidenceRepository.findAllById(List.of(evidenceId))).thenReturn(List.of(sampleEvidence));

        List<EvidenceJournalEntryResponse> journal = evidenceService.getEvidenceJournal(sessionId);

        assertEquals(1, journal.size());
        assertEquals("Cipher Note", journal.get(0).title());
        assertEquals(userId, journal.get(0).discoveredByUserId());
    }
}