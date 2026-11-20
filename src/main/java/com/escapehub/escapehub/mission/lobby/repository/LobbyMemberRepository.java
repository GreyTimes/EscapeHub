package com.escapehub.escapehub.mission.lobby.repository;

import com.escapehub.escapehub.mission.lobby.entity.LobbyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LobbyMemberRepository extends JpaRepository<LobbyMember, UUID> {

    List<LobbyMember> findByLobbyIdOrderByJoinedAtAsc(UUID lobbyId);

    boolean existsByLobbyIdAndUserId(UUID lobbyId, UUID userId);

    long countByLobbyId(UUID lobbyId);
}