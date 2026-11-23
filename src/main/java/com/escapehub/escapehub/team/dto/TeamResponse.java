
package com.escapehub.escapehub.team.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TeamResponse {

    private UUID teamId;
    private String name;
    private String joinCode;
    private UUID leaderUserId;
    private List<TeamMemberResponse> members;
    private Instant createdAt;

    public TeamResponse(
            UUID teamId,
            String name,
            String joinCode,
            UUID leaderUserId,
            List<TeamMemberResponse> members,
            Instant createdAt) {
        this.teamId = teamId;
        this.name = name;
        this.joinCode = joinCode;
        this.leaderUserId = leaderUserId;
        this.members = members;
        this.createdAt = createdAt;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public UUID getLeaderUserId() {
        return leaderUserId;
    }

    public List<TeamMemberResponse> getMembers() {
        return members;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
