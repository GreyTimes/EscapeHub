package com.escapehub.escapehub.evidence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "session_evidence_discoveries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_session_evidence_discovery",
                        columnNames = {"session_id", "evidence_id"}
                )
        }
)
public class SessionEvidenceDiscovery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    @Column(name = "discovered_by_user_id", nullable = false)
    private UUID discoveredByUserId;

    @Column(name = "discovered_at", nullable = false, updatable = false)
    private Instant discoveredAt;

    protected SessionEvidenceDiscovery() {
    }

    public SessionEvidenceDiscovery(UUID sessionId, UUID evidenceId, UUID discoveredByUserId, Instant discoveredAt) {
        this.sessionId = sessionId;
        this.evidenceId = evidenceId;
        this.discoveredByUserId = discoveredByUserId;
        this.discoveredAt = discoveredAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.discoveredAt == null) {
            this.discoveredAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getEvidenceId() {
        return evidenceId;
    }

    public UUID getDiscoveredByUserId() {
        return discoveredByUserId;
    }

    public Instant getDiscoveredAt() {
        return discoveredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionEvidenceDiscovery that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}