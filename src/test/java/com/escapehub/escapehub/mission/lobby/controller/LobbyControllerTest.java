package com.escapehub.escapehub.mission.lobby.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.mission.lobby.dto.LobbyMemberResponse;
import com.escapehub.escapehub.mission.lobby.dto.LobbyResponse;
import com.escapehub.escapehub.mission.lobby.entity.LobbyStatus;
import com.escapehub.escapehub.mission.lobby.service.LobbyService;
import com.escapehub.escapehub.mission.session.controller.SessionExceptionHandler;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.SessionMode;
import com.escapehub.escapehub.mission.session.service.SessionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class LobbyControllerTest {

    private static final String USER_ATTRIBUTE = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE;

    private LobbyService lobbyService;
    private MockMvc mockMvc;

    private final UUID teamId = UUID.randomUUID();
    private final UUID missionVersionId = UUID.randomUUID();
    private final UUID leaderId = UUID.randomUUID();
    private final UUID lobbyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lobbyService = mock(LobbyService.class);

        mockMvc = standaloneSetup(new LobbyController(lobbyService))
                .setControllerAdvice(new SessionExceptionHandler())
                .build();
    }

    @Test
    void createLobbyReturnsCreated() throws Exception {
        LobbyResponse response = new LobbyResponse(
                lobbyId,
                teamId,
                missionVersionId,
                leaderId,
                LobbyStatus.WAITING,
                Instant.now(),
                null,
                List.of(new LobbyMemberResponse(UUID.randomUUID(), leaderId, Instant.now()))
        );

        when(lobbyService.createLobby(teamId, missionVersionId, leaderId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/teams/{teamId}/lobbies", teamId)
                        .requestAttr(USER_ATTRIBUTE, leaderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"missionVersionId\":\"" + missionVersionId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/lobbies/" + lobbyId))
                .andExpect(jsonPath("$.id").value(lobbyId.toString()))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.members.length()").value(1));
    }

    @Test
    void getLobbyReturnsMembers() throws Exception {
        LobbyResponse response = new LobbyResponse(
                lobbyId,
                teamId,
                missionVersionId,
                leaderId,
                LobbyStatus.WAITING,
                Instant.now(),
                null,
                List.of(new LobbyMemberResponse(UUID.randomUUID(), leaderId, Instant.now()))
        );

        when(lobbyService.getLobby(lobbyId, leaderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/lobbies/{lobbyId}", lobbyId)
                        .requestAttr(USER_ATTRIBUTE, leaderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lobbyId.toString()))
                .andExpect(jsonPath("$.members[0].userId").value(leaderId.toString()));
    }

    @Test
    void joinLobbyReturnsUpdatedLobby() throws Exception {
        UUID memberId = UUID.randomUUID();
        LobbyResponse response = new LobbyResponse(
                lobbyId,
                teamId,
                missionVersionId,
                leaderId,
                LobbyStatus.WAITING,
                Instant.now(),
                null,
                List.of(
                        new LobbyMemberResponse(UUID.randomUUID(), leaderId, Instant.now()),
                        new LobbyMemberResponse(UUID.randomUUID(), memberId, Instant.now())
                )
        );

        when(lobbyService.joinLobby(lobbyId, memberId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/lobbies/{lobbyId}/join", lobbyId)
                        .requestAttr(USER_ATTRIBUTE, memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(2));
    }

    @Test
    void startMissionReturnsCreatedSession() throws Exception {
        MissionSession session = new MissionSession(missionVersionId, SessionMode.TEAM, teamId, leaderId);
        UUID sessionId = UUID.randomUUID();
        session.setId(sessionId);

        when(lobbyService.startMission(lobbyId, leaderId)).thenReturn(session);

        mockMvc.perform(post("/api/v1/lobbies/{lobbyId}/start", lobbyId)
                        .requestAttr(USER_ATTRIBUTE, leaderId))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/sessions/" + sessionId))
                .andExpect(jsonPath("$.mode").value("TEAM"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void startMissionWithTooFewParticipantsReturnsBadRequest() throws Exception {
        when(lobbyService.startMission(lobbyId, leaderId))
                .thenThrow(new SessionException(
                        SessionException.Reason.NOT_ENOUGH_PARTICIPANTS,
                        "At least 2 participants are required to start a team mission."
                ));

        mockMvc.perform(post("/api/v1/lobbies/{lobbyId}/start", lobbyId)
                        .requestAttr(USER_ATTRIBUTE, leaderId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}