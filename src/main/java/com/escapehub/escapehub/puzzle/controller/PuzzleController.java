package com.escapehub.escapehub.puzzle.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.puzzle.dto.PuzzleAnswerResponse;
import com.escapehub.escapehub.puzzle.dto.PuzzleResponse;
import com.escapehub.escapehub.puzzle.dto.SubmitPuzzleAnswerRequest;
import com.escapehub.escapehub.puzzle.service.PuzzleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PuzzleController {
    private final PuzzleService puzzleService;

    public PuzzleController(PuzzleService puzzleService) {
        this.puzzleService = puzzleService;
    }

    @GetMapping("/sessions/{sessionId}/nodes/{nodeId}/puzzles")
    public List<PuzzleResponse> getPuzzles(
            @PathVariable UUID sessionId,
            @PathVariable UUID nodeId,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        return puzzleService.getPuzzlesForNode(sessionId, nodeId, currentUserId);
    }

    @PostMapping("/sessions/{sessionId}/puzzles/{puzzleId}/answer")
    public PuzzleAnswerResponse submitAnswer(
            @PathVariable UUID sessionId,
            @PathVariable UUID puzzleId,
            @Valid @RequestBody SubmitPuzzleAnswerRequest request,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        return puzzleService.submitAnswer(sessionId, puzzleId, request.answer(), currentUserId);
    }
}
