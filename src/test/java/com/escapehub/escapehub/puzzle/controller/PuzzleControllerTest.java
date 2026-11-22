package com.escapehub.escapehub.puzzle.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.mission.session.controller.SessionExceptionHandler;
import com.escapehub.escapehub.mission.session.service.SessionException;
import com.escapehub.escapehub.puzzle.dto.PuzzleAnswerResponse;
import com.escapehub.escapehub.puzzle.dto.PuzzleResponse;
import com.escapehub.escapehub.puzzle.service.PuzzleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PuzzleControllerTest {
    private static final String USER_ATTRIBUTE = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE;

    private PuzzleService puzzleService;
    private MockMvc mockMvc;

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final UUID nodeId = UUID.randomUUID();
    private final UUID puzzleId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        puzzleService = mock(PuzzleService.class);

        mockMvc = standaloneSetup(new PuzzleController(puzzleService))
                .setControllerAdvice(new SessionExceptionHandler())
                .build();
    }

    @Test
    void getPuzzlesReturnsQuestionWithoutCorrectAnswer() throws Exception {
        when(puzzleService.getPuzzlesForNode(sessionId, nodeId, userId)).thenReturn(List.of(
                new PuzzleResponse(puzzleId, nodeId, "What is the capital of Greece?", true, false, 0)
        ));

        mockMvc.perform(get("/api/v1/sessions/{sessionId}/nodes/{nodeId}/puzzles", sessionId, nodeId)
                        .requestAttr(USER_ATTRIBUTE, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(puzzleId.toString()))
                .andExpect(jsonPath("$[0].question").value("What is the capital of Greece?"))
                .andExpect(jsonPath("$[0].solved").value(false))
                .andExpect(jsonPath("$[0].correctAnswer").doesNotExist())
                .andExpect(jsonPath("$[0].answer").doesNotExist());
    }

    @Test
    void correctAnswerReturnsSolvedResult() throws Exception {
        when(puzzleService.submitAnswer(sessionId, puzzleId, "Athens", userId)).thenReturn(
                new PuzzleAnswerResponse(puzzleId, true, true, 1, true, "Correct! You completed the mission.")
        );

        mockMvc.perform(post("/api/v1/sessions/{sessionId}/puzzles/{puzzleId}/answer", sessionId, puzzleId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"Athens\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.missionCompleted").value(true));
    }

    @Test
    void wrongAnswerReturnsNotCorrect() throws Exception {
        when(puzzleService.submitAnswer(sessionId, puzzleId, "Sparta", userId)).thenReturn(
                new PuzzleAnswerResponse(puzzleId, false, false, 1, false, "Wrong answer. Try again.")
        );

        mockMvc.perform(post("/api/v1/sessions/{sessionId}/puzzles/{puzzleId}/answer", sessionId, puzzleId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"Sparta\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.solved").value(false));
    }

    @Test
    void blankAnswerReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/{sessionId}/puzzles/{puzzleId}/answer", sessionId, puzzleId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("answer is required"));

        verifyNoInteractions(puzzleService);
    }

    @Test
    void unknownPuzzleReturnsNotFound() throws Exception {
        when(puzzleService.submitAnswer(any(), any(), any(), any()))
                .thenThrow(new SessionException(SessionException.Reason.PUZZLE_NOT_FOUND, "Puzzle not found."));

        mockMvc.perform(post("/api/v1/sessions/{sessionId}/puzzles/{puzzleId}/answer", sessionId, puzzleId)
                        .requestAttr(USER_ATTRIBUTE, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"Athens\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void playerInAnotherRoomGetsForbidden() throws Exception {
        when(puzzleService.getPuzzlesForNode(sessionId, nodeId, userId))
                .thenThrow(new SessionException(
                        SessionException.Reason.NOT_IN_PUZZLE_ROOM,
                        "You must be in this room to see or answer its puzzles."
                ));

        mockMvc.perform(get("/api/v1/sessions/{sessionId}/nodes/{nodeId}/puzzles", sessionId, nodeId)
                        .requestAttr(USER_ATTRIBUTE, userId))
                .andExpect(status().isForbidden());
    }
}
