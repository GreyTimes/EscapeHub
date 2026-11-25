package com.escapehub.escapehub.puzzle.repository;

import com.escapehub.escapehub.puzzle.entity.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PuzzleRepository extends JpaRepository<Puzzle, UUID> {
    List<Puzzle> findByNodeId(UUID nodeId);
    boolean existsByNodeIdAndPuzzleKey(UUID nodeId, String puzzleKey);
}
