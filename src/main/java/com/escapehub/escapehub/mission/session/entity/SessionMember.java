package com.escapehub.escapehub.mission.session.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * A player taking part in a mission session.
 * A SOLO session has exactly one member.
 */
@Entity
@Table(
        name = "session_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_session_member_session_user", columnNames = {"session_id", "user_id"})
        }
)
public class SessionMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    // Personal position on the investigation map. Set when the player enters a node.
    @Column(name = "current_node_id")
    private UUID currentNodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_status", nullable = false, length = 20)
    private ParticipationStatus participationStatus;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    public SessionMember() {
    }

    public SessionMember(UUID sessionId, UUID userId) {
        if (sessionId == null || userId == null) {
            throw new IllegalArgumentException("Session and user are required");
        }
        this.sessionId = sessionId;
        this.userId = userId;
        this.participationStatus = ParticipationStatus.ACTIVE;
    }

    @PrePersist
    protected void onJoin() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
        if (this.participationStatus == null) {
            this.participationStatus = ParticipationStatus.ACTIVE;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(UUID currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public ParticipationStatus getParticipationStatus() {
        return participationStatus;
    }

    public void setParticipationStatus(ParticipationStatus participationStatus) {
        this.participationStatus = participationStatus;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }
}
