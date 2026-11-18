package com.escapehub.escapehub.mission.session.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.mission.session.dto.SessionResponse;
import com.escapehub.escapehub.mission.session.dto.StartSessionRequest;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.service.MissionSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MissionSessionController {

    private final MissionSessionService sessionService;

    public MissionSessionController(MissionSessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * The player chooses how to play a mission.
     * SOLO starts a session immediately (201 Created).
     * TEAM is rejected here, because team sessions start from the team lobby.
     */
    @PostMapping("/missions/{missionId}/sessions")
    public ResponseEntity<SessionResponse> startSession(
            @PathVariable UUID missionId,
            @Valid @RequestBody StartSessionRequest request,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        MissionSession session = sessionService.startSession(
                request.mode(),
                request.missionVersionId(),
                currentUserId
        );

        return ResponseEntity
                .created(URI.create("/api/v1/sessions/" + session.getId()))
                .body(SessionResponse.from(session));
    }

    @GetMapping("/sessions/{sessionId}")
    public SessionResponse getSession(
            @PathVariable UUID sessionId,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        return SessionResponse.from(
                sessionService.getSessionForUser(sessionId, currentUserId)
        );
    }
}
