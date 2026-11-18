package com.escapehub.escapehub.Team.repository;

import com.escapehub.escapehub.Team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
    List<Team> findByLeaderUserId(UUID leaderUserId);
}