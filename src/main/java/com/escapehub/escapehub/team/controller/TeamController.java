
package com.escapehub.escapehub.team.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.team.dto.CreateTeamRequest;
import com.escapehub.escapehub.team.dto.JoinTeamRequest;
import com.escapehub.escapehub.team.dto.TeamResponse;
import com.escapehub.escapehub.team.entity.Team;
import com.escapehub.escapehub.team.entity.TeamMember;
import com.escapehub.escapehub.team.service.TeamService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public TeamResponse createTeam(
            @RequestBody CreateTeamRequest request,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId) {

        Team team = teamService.createTeam(
                request.getName(),
                currentUserId
        );

        return teamService.getTeamDetails(team.getId());
    }

    @PostMapping("/join")
    public TeamResponse joinTeam(
            @RequestBody JoinTeamRequest request,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId) {

        TeamMember member = teamService.joinTeam(
                request.getJoinCode(),
                currentUserId
        );

        return teamService.getTeamDetails(member.getTeamId());
    }

    @GetMapping("/{teamId}")
    public TeamResponse getTeamDetails(
            @PathVariable UUID teamId) {

        return teamService.getTeamDetails(teamId);
    }
}
