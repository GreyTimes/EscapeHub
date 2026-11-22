package com.escapehub.escapehub.mission.session.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "mission_sessions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mission_session_lobby", columnNames = "lobby_id")
        }
)
public class MissionSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "mission_version_id", nullable = false, updatable = false)
    private UUID missionVersionId;

    @Column(name = "lobby_id")
    private UUID lobbyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, updatable = false, length = 10)
    private SessionMode mode;

    @Column(name = "team_id", updatable = false)
    private UUID teamId;

    @Column(name = "started_by_user_id", nullable = false, updatable = false)
    private UUID startedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MissionSessionStatus status;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "abandoned_at")
    private Instant abandonedAt;

    public MissionSession() {
    }

    public MissionSession(UUID missionVersionId, SessionMode mode, UUID teamId, UUID startedByUserId) {
        if (missionVersionId == null) {
            throw new IllegalArgumentException("Mission version is required");
        }
        if (mode == null) {
            throw new IllegalArgumentException("Session mode is required");
        }
        if (startedByUserId == null) {
            throw new IllegalArgumentException("The user who starts the session is required");
        }
        if (mode == SessionMode.SOLO && teamId != null) {
            throw new IllegalArgumentException("A SOLO session cannot belong to a team");
        }
        if (mode == SessionMode.TEAM && teamId == null) {
            throw new IllegalArgumentException("A TEAM session must belong to a team");
        }

        this.missionVersionId = missionVersionId;
        this.mode = mode;
        this.teamId = teamId;
        this.startedByUserId = startedByUserId;
        this.status = MissionSessionStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = MissionSessionStatus.ACTIVE;
        }
    }

    public boolean isActive() {
        return status == MissionSessionStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMissionVersionId() {
        return missionVersionId;
    }

    public UUID getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(UUID lobbyId) {
        this.lobbyId = lobbyId;
    }

    public SessionMode getMode() {
        return mode;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getStartedByUserId() {
        return startedByUserId;
    }

    public MissionSessionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionSessionStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getAbandonedAt() {
        return abandonedAt;
    }

    public void setAbandonedAt(Instant abandonedAt) {
        this.abandonedAt = abandonedAt;
    }
}
