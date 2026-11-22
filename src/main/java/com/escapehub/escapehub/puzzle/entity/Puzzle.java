package com.escapehub.escapehub.puzzle.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
        name = "puzzles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_puzzle_node_key", columnNames = {"node_id", "puzzle_key"})
        }
)
public class Puzzle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "puzzle_key", nullable = false, length = 100)
    private String puzzleKey;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @Column(name = "final_puzzle", nullable = false)
    private boolean finalPuzzle;

    public Puzzle() {
    }

    public Puzzle(UUID nodeId, String puzzleKey, String question, String correctAnswer, boolean finalPuzzle) {
        this.nodeId = nodeId;
        this.puzzleKey = puzzleKey;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.finalPuzzle = finalPuzzle;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    public String getPuzzleKey() {
        return puzzleKey;
    }

    public void setPuzzleKey(String puzzleKey) {
        this.puzzleKey = puzzleKey;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public boolean isFinalPuzzle() {
        return finalPuzzle;
    }

    public void setFinalPuzzle(boolean finalPuzzle) {
        this.finalPuzzle = finalPuzzle;
    }
}
