
package com.escapehub.escapehub.team.service;
import com.escapehub.escapehub.team.entity.Team;
import com.escapehub.escapehub.team.entity.TeamMember;
import com.escapehub.escapehub.team.entity.TeamMemberRole;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.escapehub.escapehub.team.repository.TeamMemberRepository;
import com.escapehub.escapehub.team.repository.TeamRepository;

import org.springframework.stereotype.Service;

import com.escapehub.escapehub.user.repository.UserRepository;
import com.escapehub.escapehub.user.entity.User;

import com.escapehub.escapehub.team.dto.TeamResponse;
import com.escapehub.escapehub.team.dto.TeamMemberResponse;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository) {

        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Team createTeam(String name, UUID userId) {

        String joinCode = generateJoinCode();

        Team team = new Team(name, joinCode, userId);
        team = teamRepository.save(team);

        TeamMember leader = new TeamMember(
                team.getId(),
                userId,
                TeamMemberRole.LEADER
        );

        teamMemberRepository.save(leader);

        return team;
    }

    private String generateJoinCode() {

        String joinCode;

        do {
            joinCode = UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();

        } while (teamRepository.existsByJoinCode(joinCode));

        return joinCode;
    }
    //TODO: Replace RuntimeException με Custom Exceptions απο τον Controller ως HTTP responses
    @Transactional
    public TeamMember joinTeam(String joinCode, UUID userId) {

        Team team = teamRepository.findByJoinCode(joinCode)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        if (teamMemberRepository.existsByTeamIdAndUserId(
                team.getId(), userId)) {
            throw new RuntimeException("User is already a member");
        }

        if (teamMemberRepository.countByTeamId(team.getId())
                >= team.getMaxMembers()) {
            throw new RuntimeException("Team is full");
        }

        TeamMember member = new TeamMember(
                team.getId(),
                userId,
                TeamMemberRole.MEMBER
        );

        return teamMemberRepository.save(member);
    }


    public TeamResponse getTeamDetails(UUID teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        List<TeamMemberResponse> members =
                teamMemberRepository.findByTeamId(teamId)
                        .stream()
                        .map(member -> {

                            User user = userRepository
                                    .findById(member.getUserId())
                                    .orElseThrow(() ->
                                            new RuntimeException("User not found"));

                            return new TeamMemberResponse(
                                    user.getId(),
                                    user.getDisplayName(),
                                    member.getRole(),
                                    member.getJoinedAt()
                            );
                        })
                        .toList();

        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getJoinCode(),
                team.getLeaderUserId(),
                members,
                team.getCreatedAt()
        );
    }



}
