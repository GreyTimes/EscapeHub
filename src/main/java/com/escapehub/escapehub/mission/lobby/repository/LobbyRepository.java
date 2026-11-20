package com.escapehub.escapehub.mission.lobby.repository;

import com.escapehub.escapehub.mission.lobby.entity.Lobby;
import com.escapehub.escapehub.mission.lobby.entity.LobbyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LobbyRepository extends JpaRepository<Lobby, UUID> {

    boolean existsByTeamIdAndStatus(UUID teamId, LobbyStatus status);
}