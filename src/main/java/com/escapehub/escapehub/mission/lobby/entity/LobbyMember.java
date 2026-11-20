package com.escapehub.escapehub.mission.lobby.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "lobby_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_lobby_member_lobby_user", columnNames = {"lobby_id", "user_id"})
        }
)
public class LobbyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "lobby_id", nullable = false, updatable = false)
    private UUID lobbyId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    public LobbyMember() {
    }

    public LobbyMember(UUID lobbyId, UUID userId) {
        if (lobbyId == null || userId == null) {
            throw new IllegalArgumentException("Lobby and user are required");
        }
        this.lobbyId = lobbyId;
        this.userId = userId;
    }

    @PrePersist
    protected void onJoin() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getLobbyId() {
        return lobbyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}