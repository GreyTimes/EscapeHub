package com.escapehub.escapehub.puzzle.dto;

import java.util.UUID;

public record PuzzleAnswerResponse(
        UUID puzzleId,
        boolean correct,
        boolean solved,
        int attemptsUsed,
        boolean missionCompleted,
        String message
) {
}
