
package com.escapehub.escapehub.team.dto;

import com.escapehub.escapehub.team.entity.TeamMemberRole;

import java.time.Instant;
import java.util.UUID;

public class TeamMemberResponse {

    private UUID userId;
    private String displayName;
    private TeamMemberRole role;
    private Instant joinedAt;

    public TeamMemberResponse(
            UUID userId,
            String displayName,
            TeamMemberRole role,
            Instant joinedAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TeamMemberRole getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
