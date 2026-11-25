package com.escapehub.escapehub.mission.lobby.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.mission.lobby.dto.CreateLobbyRequest;
import com.escapehub.escapehub.mission.lobby.dto.JoinLobbyRequest;
import com.escapehub.escapehub.mission.lobby.dto.LobbyResponse;
import com.escapehub.escapehub.mission.lobby.service.LobbyService;
import com.escapehub.escapehub.mission.session.dto.SessionResponse;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping("/teams/{teamId}/lobbies")
    public ResponseEntity<LobbyResponse> createLobby(
            @PathVariable UUID teamId,
            @Valid @RequestBody CreateLobbyRequest request,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        LobbyResponse lobby = lobbyService.createLobby(teamId, request.missionVersionId(), currentUserId);

        return ResponseEntity
                .created(URI.create("/api/v1/lobbies/" + lobby.id()))
                .body(lobby);
    }

    @GetMapping("/lobbies/{lobbyId}")
    public LobbyResponse getLobby(
            @PathVariable UUID lobbyId,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        return lobbyService.getLobby(lobbyId, currentUserId);
    }

    @GetMapping("/teams/{teamId}/lobbies/waiting")
    public LobbyResponse getWaitingLobbyForTeam(
            @PathVariable UUID teamId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId
    ) {
        return lobbyService.getWaitingLobbyForTeam(
                teamId, currentUserId
        );
    }

    @PostMapping("/lobbies/{lobbyId}/join")
    public LobbyResponse joinLobby(
            @PathVariable UUID lobbyId,
            @RequestBody(required = false) JoinLobbyRequest request,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        return lobbyService.joinLobby(lobbyId, currentUserId);
    }

    @PostMapping("/lobbies/{lobbyId}/start")
    public ResponseEntity<SessionResponse> startMission(
            @PathVariable UUID lobbyId,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        MissionSession session = lobbyService.startMission(lobbyId, currentUserId);

        return ResponseEntity
                .created(URI.create("/api/v1/sessions/" + session.getId()))
                .body(SessionResponse.from(session));
    }
}