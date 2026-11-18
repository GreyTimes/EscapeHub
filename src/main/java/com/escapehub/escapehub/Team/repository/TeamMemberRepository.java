package com.escapehub.escapehub.Team.repository;

import com.escapehub.escapehub.Team.entity.TeamMember;
import com.escapehub.escapehub.Team.entity.TeamMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamId(UUID teamId);
    List<TeamMember> findByUserId(UUID userId);
    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);
    long countByTeamId(UUID teamId);
    Optional<TeamMember> findByTeamIdAndRole(UUID teamId, TeamMemberRole role);
}