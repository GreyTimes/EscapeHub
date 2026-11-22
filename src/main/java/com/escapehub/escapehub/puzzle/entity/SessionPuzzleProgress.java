package com.escapehub.escapehub.puzzle.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "session_puzzle_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_puzzle_progress_session_puzzle", columnNames = {"session_id", "puzzle_id"})
        }
)
public class SessionPuzzleProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @Column(name = "puzzle_id", nullable = false, updatable = false)
    private UUID puzzleId;

    @Column(name = "solved", nullable = false)
    private boolean solved;

    @Column(name = "attempts_used", nullable = false)
    private int attemptsUsed;

    @Column(name = "solved_by_user_id")
    private UUID solvedByUserId;

    @Column(name = "solved_at")
    private Instant solvedAt;

    public SessionPuzzleProgress() {
    }

    public SessionPuzzleProgress(UUID sessionId, UUID puzzleId) {
        this.sessionId = sessionId;
        this.puzzleId = puzzleId;
        this.solved = false;
        this.attemptsUsed = 0;
    }

    public void recordAttempt() {
        this.attemptsUsed++;
    }

    public void markSolved(UUID userId) {
        this.solved = true;
        this.solvedByUserId = userId;
        this.solvedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getPuzzleId() {
        return puzzleId;
    }

    public boolean isSolved() {
        return solved;
    }

    public int getAttemptsUsed() {
        return attemptsUsed;
    }

    public UUID getSolvedByUserId() {
        return solvedByUserId;
    }

    public Instant getSolvedAt() {
        return solvedAt;
    }
}
