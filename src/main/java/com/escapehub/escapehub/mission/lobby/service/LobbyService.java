package com.escapehub.escapehub.mission.lobby.service;

import com.escapehub.escapehub.mission.lobby.dto.LobbyResponse;
import com.escapehub.escapehub.mission.lobby.entity.Lobby;
import com.escapehub.escapehub.mission.lobby.entity.LobbyMember;
import com.escapehub.escapehub.mission.lobby.entity.LobbyStatus;
import com.escapehub.escapehub.mission.lobby.repository.LobbyMemberRepository;
import com.escapehub.escapehub.mission.lobby.repository.LobbyRepository;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
import com.escapehub.escapehub.mission.session.service.MissionSessionService;
import com.escapehub.escapehub.mission.session.service.SessionException;
import com.escapehub.escapehub.team.entity.Team;
import com.escapehub.escapehub.team.repository.TeamMemberRepository;
import com.escapehub.escapehub.team.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LobbyService {

    private final LobbyRepository lobbyRepository;
    private final LobbyMemberRepository lobbyMemberRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MissionSessionRepository missionSessionRepository;
    private final SessionMemberRepository sessionMemberRepository;
    private final MissionSessionService missionSessionService;

    public LobbyService(
            LobbyRepository lobbyRepository,
            LobbyMemberRepository lobbyMemberRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            MissionSessionRepository missionSessionRepository,
            SessionMemberRepository sessionMemberRepository,
            MissionSessionService missionSessionService
    ) {
        this.lobbyRepository = lobbyRepository;
        this.lobbyMemberRepository = lobbyMemberRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.missionSessionRepository = missionSessionRepository;
        this.sessionMemberRepository = sessionMemberRepository;
        this.missionSessionService = missionSessionService;
    }

    @Transactional
    public LobbyResponse createLobby(UUID teamId, UUID missionVersionId, UUID currentUserId) {
        requireUser(currentUserId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.TEAM_NOT_FOUND,
                        "Team not found."
                ));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new SessionException(
                    SessionException.Reason.FORBIDDEN_ACTION,
                    "Only the team leader can create a lobby."
            );
        }

        if (lobbyRepository.existsByTeamIdAndStatus(teamId, LobbyStatus.WAITING)) {
            throw new SessionException(
                    SessionException.Reason.ACTIVE_LOBBY_EXISTS,
                    "This team already has an open lobby."
            );
        }

        if (missionSessionRepository.existsByTeamIdAndStatus(teamId, MissionSessionStatus.ACTIVE)) {
            throw new SessionException(
                    SessionException.Reason.ACTIVE_SESSION_EXISTS,
                    "This team already has an active mission."
            );
        }

        if (sessionMemberRepository.existsByUserIdAndSessionStatus(currentUserId, MissionSessionStatus.ACTIVE)) {
            throw new SessionException(
                    SessionException.Reason.ACTIVE_SESSION_EXISTS,
                    "You already have an active mission."
            );
        }

        Lobby lobby = lobbyRepository.save(new Lobby(teamId, missionVersionId, currentUserId));
        LobbyMember leaderMember = lobbyMemberRepository.save(new LobbyMember(lobby.getId(), currentUserId));

        return LobbyResponse.from(lobby, List.of(leaderMember));
    }

    @Transactional(readOnly = true)
    public LobbyResponse getLobby(UUID lobbyId, UUID currentUserId) {
        requireUser(currentUserId);

        Lobby lobby = findLobbyOrThrow(lobbyId);

        boolean isTeamMember = lobby.getCreatedByUserId().equals(currentUserId)
                || teamMemberRepository.existsByTeamIdAndUserId(lobby.getTeamId(), currentUserId);

        if (!isTeamMember) {
            throw new SessionException(
                    SessionException.Reason.FORBIDDEN_ACTION,
                    "Only team members can view this lobby."
            );
        }

        List<LobbyMember> members = lobbyMemberRepository.findByLobbyIdOrderByJoinedAtAsc(lobbyId);
        return LobbyResponse.from(lobby, members);
    }

    @Transactional(readOnly = true)
    public LobbyResponse getWaitingLobbyForTeam(
            UUID teamId,
            UUID currentUserId
    ) {
        requireUser(currentUserId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.TEAM_NOT_FOUND,
                        "Team not found."
                ));

        if (!teamMemberRepository.existsByTeamIdAndUserId(
                teamId, currentUserId
        )) {
            throw new SessionException(
                    SessionException.Reason.FORBIDDEN_ACTION,
                    "Only team members can view this lobby."
            );
        }

        Lobby lobby = lobbyRepository.findByTeamIdAndStatus(
                        teamId, LobbyStatus.WAITING
                )
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.LOBBY_NOT_FOUND,
                        "This team does not have a waiting lobby."
                ));

        List<LobbyMember> members =
                lobbyMemberRepository.findByLobbyIdOrderByJoinedAtAsc(
                        lobby.getId()
                );

        return LobbyResponse.from(lobby, members);
    }

    @Transactional
    public LobbyResponse joinLobby(UUID lobbyId, UUID currentUserId) {
        requireUser(currentUserId);

        Lobby lobby = findLobbyOrThrow(lobbyId);

        if (!lobby.isWaiting()) {
            throw new SessionException(
                    SessionException.Reason.LOBBY_NOT_WAITING,
                    "This lobby is no longer accepting participants."
            );
        }

        if (!teamMemberRepository.existsByTeamIdAndUserId(lobby.getTeamId(), currentUserId)) {
            throw new SessionException(
                    SessionException.Reason.FORBIDDEN_ACTION,
                    "Only members of this team can join the lobby."
            );
        }

        if (lobbyMemberRepository.existsByLobbyIdAndUserId(lobbyId, currentUserId)) {
            throw new SessionException(
                    SessionException.Reason.ALREADY_IN_LOBBY,
                    "You are already in this lobby."
            );
        }

        if (sessionMemberRepository.existsByUserIdAndSessionStatus(currentUserId, MissionSessionStatus.ACTIVE)) {
            throw new SessionException(
                    SessionException.Reason.ACTIVE_SESSION_EXISTS,
                    "You already have an active mission."
            );
        }

        if (lobbyMemberRepository.countByLobbyId(lobbyId) >= Lobby.MAX_PARTICIPANTS) {
            throw new SessionException(
                    SessionException.Reason.LOBBY_FULL,
                    "This lobby already has the maximum number of participants (" + Lobby.MAX_PARTICIPANTS + ")."
            );
        }

        lobbyMemberRepository.save(new LobbyMember(lobbyId, currentUserId));
        List<LobbyMember> members = lobbyMemberRepository.findByLobbyIdOrderByJoinedAtAsc(lobbyId);

        return LobbyResponse.from(lobby, members);
    }

    @Transactional
    public MissionSession startMission(UUID lobbyId, UUID currentUserId) {
        requireUser(currentUserId);

        Lobby lobby = findLobbyOrThrow(lobbyId);

        if (!lobby.isWaiting()) {
            throw new SessionException(
                    SessionException.Reason.LOBBY_NOT_WAITING,
                    "This lobby has already started."
            );
        }

        if (!lobby.getCreatedByUserId().equals(currentUserId)) {
            throw new SessionException(
                    SessionException.Reason.FORBIDDEN_ACTION,
                    "Only the team leader can start the mission."
            );
        }

        List<LobbyMember> members = lobbyMemberRepository.findByLobbyIdOrderByJoinedAtAsc(lobbyId);

        if (members.size() < Lobby.MIN_PARTICIPANTS) {
            throw new SessionException(
                    SessionException.Reason.NOT_ENOUGH_PARTICIPANTS,
                    "At least " + Lobby.MIN_PARTICIPANTS + " participants are required to start a team mission."
            );
        }

        if (members.size() > Lobby.MAX_PARTICIPANTS) {
            throw new SessionException(
                    SessionException.Reason.LOBBY_FULL,
                    "A team mission cannot have more than " + Lobby.MAX_PARTICIPANTS + " participants."
            );
        }

        List<UUID> participantUserIds = members.stream()
                .map(LobbyMember::getUserId)
                .toList();

        MissionSession session = missionSessionService.startTeamSession(
                lobby.getMissionVersionId(),
                lobby.getTeamId(),
                lobby.getId(),
                currentUserId,
                participantUserIds
        );

        lobby.setStatus(LobbyStatus.STARTED);
        lobby.setStartedAt(Instant.now());
        lobbyRepository.save(lobby);

        return session;
    }

    private Lobby findLobbyOrThrow(UUID lobbyId) {
        return lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.LOBBY_NOT_FOUND,
                        "Lobby not found."
                ));
    }

    private void requireUser(UUID userId) {
        if (userId == null) {
            throw new SessionException(
                    SessionException.Reason.MISSING_CURRENT_USER,
                    "A current user is required."
            );
        }
    }
}