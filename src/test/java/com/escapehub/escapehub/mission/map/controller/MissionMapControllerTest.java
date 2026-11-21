package com.escapehub.escapehub.mission.map.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.mission.map.dto.MapNodeResponse;
import com.escapehub.escapehub.mission.map.dto.MapResponse;
import com.escapehub.escapehub.mission.map.service.MissionMapService;
import com.escapehub.escapehub.mission.session.controller.SessionExceptionHandler;
import com.escapehub.escapehub.mission.session.service.SessionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class MissionMapControllerTest {

    private static final String USER_ATTRIBUTE = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE;

    private MissionMapService missionMapService;
    private MockMvc mockMvc;

    private final UUID sessionId = UUID.randomUUID();
    private final UUID missionVersionId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID startNodeId = UUID.randomUUID();
    private final UUID targetNodeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        missionMapService = mock(MissionMapService.class);

        mockMvc = standaloneSetup(new MissionMapController(missionMapService))
                .setControllerAdvice(new SessionExceptionHandler())
                .build();
    }

    private MapResponse sampleMap(UUID currentNodeId) {
        return new MapResponse(
                sessionId,
                missionVersionId,
                currentNodeId,
                List.of(
                        new MapNodeResponse(startNodeId, "entry", "Entry Hall", "Start", 100, 100, true, List.of(targetNodeId)),
                        new MapNodeResponse(targetNodeId, "library", "Library", "Books", 300, 150, false, List.of(startNodeId))
                ),
                List.of(new MapResponse.Connection(startNodeId, targetNodeId))
        );
    }

    @Test
    void getMapReturnsRoomsAndCurrentLocation() throws Exception {
        when(missionMapService.getMap(sessionId, userId)).thenReturn(sampleMap(startNodeId));

        mockMvc.perform(get("/api/v1/sessions/{sessionId}/map", sessionId)
                        .requestAttr(USER_ATTRIBUTE, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.currentNodeId").value(startNodeId.toString()))
                .andExpect(jsonPath("$.nodes.length()").value(2))
                .andExpect(jsonPath("$.connections.length()").value(1));
    }

    @Test
    void movePlayerReturnsUpdatedCurrentNodeId() throws Exception {
        when(missionMapService.movePlayer(sessionId, targetNodeId, userId))
                .thenReturn(sampleMap(targetNodeId));

        mockMvc.perform(patch("/api/v1/sessions/{sessionId}/location", sessionId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetNodeId\":\"" + targetNodeId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentNodeId").value(targetNodeId.toString()));
    }

    @Test
    void movePlayerToDisconnectedNodeReturnsBadRequest() throws Exception {
        when(missionMapService.movePlayer(sessionId, targetNodeId, userId))
                .thenThrow(new SessionException(
                        SessionException.Reason.NODE_NOT_CONNECTED,
                        "You can only move to a room connected to your current location."
                ));

        mockMvc.perform(patch("/api/v1/sessions/{sessionId}/location", sessionId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetNodeId\":\"" + targetNodeId + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void movePlayerWithoutTargetNodeIdReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/sessions/{sessionId}/location", sessionId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("targetNodeId is required"));

        verifyNoInteractions(missionMapService);
    }

    @Test
    void nonMemberCannotGetMapReturnsForbidden() throws Exception {
        when(missionMapService.getMap(sessionId, userId))
                .thenThrow(new SessionException(
                        SessionException.Reason.NOT_A_SESSION_MEMBER,
                        "You are not a member of this session."
                ));

        mockMvc.perform(get("/api/v1/sessions/{sessionId}/map", sessionId)
                        .requestAttr(USER_ATTRIBUTE, userId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }
}