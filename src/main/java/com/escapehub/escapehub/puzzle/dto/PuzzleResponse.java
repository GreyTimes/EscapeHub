package com.escapehub.escapehub.puzzle.dto;

import com.escapehub.escapehub.puzzle.entity.Puzzle;
import com.escapehub.escapehub.puzzle.entity.SessionPuzzleProgress;

import java.util.UUID;

public record PuzzleResponse(
        UUID id,
        UUID nodeId,
        String question,
        boolean finalPuzzle,
        boolean solved,
        int attemptsUsed
) {
    public static PuzzleResponse from(Puzzle puzzle, SessionPuzzleProgress progress) {
        return new PuzzleResponse(
                puzzle.getId(),
                puzzle.getNodeId(),
                puzzle.getQuestion(),
                puzzle.isFinalPuzzle(),
                progress != null && progress.isSolved(),
                progress != null ? progress.getAttemptsUsed() : 0
        );
    }
}
