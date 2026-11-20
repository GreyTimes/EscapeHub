package com.escapehub.escapehub.mission.lobby.dto;

import com.escapehub.escapehub.mission.lobby.entity.Lobby;
import com.escapehub.escapehub.mission.lobby.entity.LobbyMember;
import com.escapehub.escapehub.mission.lobby.entity.LobbyStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LobbyResponse(
        UUID id,
        UUID teamId,
        UUID missionVersionId,
        UUID createdByUserId,
        LobbyStatus status,
        Instant createdAt,
        Instant startedAt,
        List<LobbyMemberResponse> members
) {
    public static LobbyResponse from(Lobby lobby, List<LobbyMember> members) {
        List<LobbyMemberResponse> memberResponses = members.stream()
                .map(LobbyMemberResponse::from)
                .toList();

        return new LobbyResponse(
                lobby.getId(),
                lobby.getTeamId(),
                lobby.getMissionVersionId(),
                lobby.getCreatedByUserId(),
                lobby.getStatus(),
                lobby.getCreatedAt(),
                lobby.getStartedAt(),
                memberResponses
        );
    }
}