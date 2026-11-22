package com.escapehub.escapehub.puzzle.repository;

import com.escapehub.escapehub.puzzle.entity.SessionPuzzleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionPuzzleProgressRepository extends JpaRepository<SessionPuzzleProgress, UUID> {
    Optional<SessionPuzzleProgress> findBySessionIdAndPuzzleId(UUID sessionId, UUID puzzleId);

    List<SessionPuzzleProgress> findBySessionIdAndPuzzleIdIn(UUID sessionId, Collection<UUID> puzzleIds);
}
