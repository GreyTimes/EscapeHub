package com.escapehub.escapehub.mission.lobby.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lobbies")
public class Lobby {

    public static final int MIN_PARTICIPANTS = 2;
    public static final int MAX_PARTICIPANTS = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "team_id", nullable = false, updatable = false)
    private UUID teamId;

    @Column(name = "mission_version_id", nullable = false, updatable = false)
    private UUID missionVersionId;

    @Column(name = "created_by_user_id", nullable = false, updatable = false)
    private UUID createdByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LobbyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    public Lobby() {
    }

    public Lobby(UUID teamId, UUID missionVersionId, UUID createdByUserId) {
        if (teamId == null) {
            throw new IllegalArgumentException("Team is required");
        }
        if (missionVersionId == null) {
            throw new IllegalArgumentException("Mission version is required");
        }
        if (createdByUserId == null) {
            throw new IllegalArgumentException("Creator user is required");
        }
        this.teamId = teamId;
        this.missionVersionId = missionVersionId;
        this.createdByUserId = createdByUserId;
        this.status = LobbyStatus.WAITING;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = LobbyStatus.WAITING;
        }
    }

    public boolean isWaiting() {
        return this.status == LobbyStatus.WAITING;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getMissionVersionId() {
        return missionVersionId;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public LobbyStatus getStatus() {
        return status;
    }

    public void setStatus(LobbyStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }
}