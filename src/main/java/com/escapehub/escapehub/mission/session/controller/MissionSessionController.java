
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MissionSessionController {

    private final MissionSessionService sessionService;

    public MissionSessionController(MissionSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/missions/{missionId}/sessions")
    public ResponseEntity<SessionResponse> startSession(
            @PathVariable UUID missionId,
            @Valid @RequestBody StartSessionRequest request,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            )
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
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            )
            UUID currentUserId
    ) {
        return SessionResponse.from(
                sessionService.getSessionForUser(sessionId, currentUserId)
        );
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<SessionResponse> getActiveSession(
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            )
            UUID currentUserId
    ) {
        Optional<MissionSession> activeSession =
                sessionService.getActiveSessionForUser(currentUserId);

        if (activeSession.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                SessionResponse.from(activeSession.get())
        );
    }

    @PostMapping("/sessions/{sessionId}/abandon")
    public SessionResponse abandonSession(
            @PathVariable UUID sessionId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            )
            UUID currentUserId
    ) {
        return SessionResponse.from(
                sessionService.abandonSession(sessionId, currentUserId)
        );
    }
}
