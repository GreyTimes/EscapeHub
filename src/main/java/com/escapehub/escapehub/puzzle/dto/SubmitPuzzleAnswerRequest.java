package com.escapehub.escapehub.puzzle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitPuzzleAnswerRequest(
        @NotBlank(message = "answer is required")
        @Size(max = 255, message = "answer must be at most 255 characters")
        String answer
) {
}
