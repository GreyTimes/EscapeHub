package com.escapehub.escapehub.team.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_team_join_code", columnNames = "join_code")
        }
)
public class Team {

    public static final int MAX_TEAM_SIZE = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "join_code", nullable = false, unique = true, length = 16)
    private String joinCode;

    @Column(name = "leader_user_id", nullable = false)
    private UUID leaderUserId;

    @Column(name = "max_members", nullable = false)
    private int maxMembers = MAX_TEAM_SIZE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Team() {
    }

    public Team(String name, String joinCode, UUID leaderUserId) {
        this.name = name;
        this.joinCode = joinCode;
        this.leaderUserId = leaderUserId;
        this.maxMembers = MAX_TEAM_SIZE;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.maxMembers <= 0 || this.maxMembers > MAX_TEAM_SIZE) {
            this.maxMembers = MAX_TEAM_SIZE;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public UUID getLeaderUserId() {
        return leaderUserId;
    }

    public void setLeaderUserId(UUID leaderUserId) {
        this.leaderUserId = leaderUserId;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        if (maxMembers < 2 || maxMembers > MAX_TEAM_SIZE) {
            throw new IllegalArgumentException("Team size must be between 2 and " + MAX_TEAM_SIZE);
        }
        this.maxMembers = maxMembers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}