package com.escaphub.escapehub.team.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "team_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_team_member_team_user", columnNames = {"team_id", "user_id"})
        }
)
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private TeamMemberRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    public TeamMember() {
    }

    public TeamMember(UUID teamId, UUID userId, TeamMemberRole role) {
        this.teamId = teamId;
        this.userId = userId;
        this.role = role != null ? role : TeamMemberRole.MEMBER;
    }

    @PrePersist
    protected void onJoin() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
        if (this.role == null) {
            this.role = TeamMemberRole.MEMBER;
        }
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

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public TeamMemberRole getRole() {
        return role;
    }

    public void setRole(TeamMemberRole role) {
        this.role = role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}