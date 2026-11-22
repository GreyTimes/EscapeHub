package com.escapehub.escapehub.mission.session.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.SessionMode;
import com.escapehub.escapehub.mission.session.service.MissionSessionService;
import com.escapehub.escapehub.mission.session.service.SessionException;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class MissionSessionControllerTest {
    private static final String USER_ATTRIBUTE = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE;

    private MissionSessionService sessionService;
    private MockMvc mockMvc;

    private final UUID userId = UUID.randomUUID();
    private final UUID missionId = UUID.randomUUID();
    private final UUID missionVersionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sessionService = mock(MissionSessionService.class);

        mockMvc = standaloneSetup(new MissionSessionController(sessionService))
                .setControllerAdvice(new SessionExceptionHandler())
                .build();
    }

    private String body(String mode) {
        return "{\"mode\":\"" + mode + "\",\"missionVersionId\":\"" + missionVersionId + "\"}";
    }

    @Test
    void soloStartReturnsCreatedSession() throws Exception {
        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        UUID sessionId = UUID.randomUUID();
        session.setId(sessionId);

        when(sessionService.startSession(SessionMode.SOLO, missionVersionId, userId)).thenReturn(session);

        mockMvc.perform(post("/api/v1/missions/{missionId}/sessions", missionId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("SOLO")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/sessions/" + sessionId))
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.mode").value("SOLO"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.missionVersionId").value(missionVersionId.toString()));
    }

    @Test
    void teamModeReturnsBadRequestWithMessage() throws Exception {
        when(sessionService.startSession(eq(SessionMode.TEAM), any(), any()))
                .thenThrow(new SessionException(
                        SessionException.Reason.TEAM_MODE_REQUIRES_LOBBY,
                        "Team missions are started by the team leader from a team lobby."
                ));

        mockMvc.perform(post("/api/v1/missions/{missionId}/sessions", missionId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("TEAM")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void secondActiveSessionReturnsConflict() throws Exception {
        when(sessionService.startSession(any(), any(), any()))
                .thenThrow(new SessionException(
                        SessionException.Reason.ACTIVE_SESSION_EXISTS,
                        "You already have an active mission."
                ));

        mockMvc.perform(post("/api/v1/missions/{missionId}/sessions", missionId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("SOLO")))
                .andExpect(status().isConflict());
    }

    @Test
    void missingUserReturnsUnauthorized() throws Exception {
        when(sessionService.startSession(any(), any(), isNull()))
                .thenThrow(new SessionException(
                        SessionException.Reason.MISSING_CURRENT_USER,
                        "A current user is required."
                ));

        mockMvc.perform(post("/api/v1/missions/{missionId}/sessions", missionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("SOLO")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownModeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/missions/{missionId}/sessions", missionId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("DUO")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(sessionService);
    }

    @Test
    void missingModeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/missions/{missionId}/sessions", missionId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"missionVersionId\":\"" + missionVersionId + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("mode is required (SOLO or TEAM)"));

        verifyNoInteractions(sessionService);
    }

    @Test
    void nonMemberGetsForbiddenWhenViewingSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        when(sessionService.getSessionForUser(sessionId, userId))
                .thenThrow(new SessionException(
                        SessionException.Reason.NOT_A_SESSION_MEMBER,
                        "You are not a member of this session."
                ));

        mockMvc.perform(get("/api/v1/sessions/{sessionId}", sessionId)
                        .requestAttr(USER_ATTRIBUTE, userId))
                .andExpect(status().isForbidden());
    }
    @Test
    void abandonSessionReturnsAbandonedStatus() throws Exception {
        UUID sessionId = UUID.randomUUID();
        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);
        session.setStatus(MissionSessionStatus.ABANDONED);

        when(sessionService.abandonSession(sessionId, userId)).thenReturn(session);

        mockMvc.perform(post("/api/v1/sessions/{sessionId}/abandon", sessionId)
                        .requestAttr(USER_ATTRIBUTE, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.status").value("ABANDONED"));
    }

    @Test
    void nonOwnerAbandonReturnsForbidden() throws Exception {
        UUID sessionId = UUID.randomUUID();
        when(sessionService.abandonSession(sessionId, userId))
                .thenThrow(new SessionException(
                        SessionException.Reason.FORBIDDEN_ACTION,
                        "Only the session owner or team leader can abandon this session."
                ));

        mockMvc.perform(post("/api/v1/sessions/{sessionId}/abandon", sessionId)
                        .requestAttr(USER_ATTRIBUTE, userId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }
}
