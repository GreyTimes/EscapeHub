package com.escapehub.escapehub.mission.lobby.dto;

import com.escapehub.escapehub.mission.lobby.entity.LobbyMember;

import java.time.Instant;
import java.util.UUID;

public record LobbyMemberResponse(
        UUID id,
        UUID userId,
        Instant joinedAt
) {
    public static LobbyMemberResponse from(LobbyMember member) {
        return new LobbyMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getJoinedAt()
        );
    }
}