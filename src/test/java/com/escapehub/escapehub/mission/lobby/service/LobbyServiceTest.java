package com.escapehub.escapehub.mission.lobby.service;

import com.escapehub.escapehub.mission.lobby.dto.LobbyResponse;
import com.escapehub.escapehub.mission.lobby.entity.Lobby;
import com.escapehub.escapehub.mission.lobby.entity.LobbyMember;
import com.escapehub.escapehub.mission.lobby.entity.LobbyStatus;
import com.escapehub.escapehub.mission.lobby.repository.LobbyMemberRepository;
import com.escapehub.escapehub.mission.lobby.repository.LobbyRepository;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMode;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
import com.escapehub.escapehub.mission.session.service.MissionSessionService;
import com.escapehub.escapehub.mission.session.service.SessionException;
import com.escapehub.escapehub.team.entity.Team;
import com.escapehub.escapehub.team.repository.TeamMemberRepository;
import com.escapehub.escapehub.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyServiceTest {

    @Mock
    private LobbyRepository lobbyRepository;
    @Mock
    private LobbyMemberRepository lobbyMemberRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private MissionSessionRepository missionSessionRepository;
    @Mock
    private SessionMemberRepository sessionMemberRepository;
    @Mock
    private MissionSessionService missionSessionService;

    private LobbyService lobbyService;

    private final UUID teamId = UUID.randomUUID();
    private final UUID missionVersionId = UUID.randomUUID();
    private final UUID leaderId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lobbyService = new LobbyService(
                lobbyRepository,
                lobbyMemberRepository,
                teamRepository,
                teamMemberRepository,
                missionSessionRepository,
                sessionMemberRepository,
                missionSessionService
        );
    }

    @Test
    void leaderCanCreateLobbyAndIsAddedAsFirstMember() {
        Team team = new Team("Detectives", "DET123", leaderId);
        team.setId(teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(lobbyRepository.existsByTeamIdAndStatus(teamId, LobbyStatus.WAITING)).thenReturn(false);
        when(missionSessionRepository.existsByTeamIdAndStatus(teamId, MissionSessionStatus.ACTIVE)).thenReturn(false);
        when(sessionMemberRepository.existsByUserIdAndSessionStatus(leaderId, MissionSessionStatus.ACTIVE)).thenReturn(false);
        when(lobbyRepository.save(any(Lobby.class))).thenAnswer(invocation -> {
            Lobby saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(lobbyMemberRepository.save(any(LobbyMember.class))).thenAnswer(invocation -> {
            LobbyMember saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        LobbyResponse response = lobbyService.createLobby(teamId, missionVersionId, leaderId);

        assertNotNull(response.id());
        assertEquals(teamId, response.teamId());
        assertEquals(LobbyStatus.WAITING, response.status());
        assertEquals(1, response.members().size());
        assertEquals(leaderId, response.members().getFirst().userId());
    }

    @Test
    void nonLeaderCannotCreateLobby() {
        Team team = new Team("Detectives", "DET123", leaderId);
        team.setId(teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        SessionException exception = assertThrows(
                SessionException.class,
                () -> lobbyService.createLobby(teamId, missionVersionId, memberId)
        );

        assertEquals(SessionException.Reason.FORBIDDEN_ACTION, exception.getReason());
        verify(lobbyRepository, never()).save(any());
    }

    @Test
    void teamMemberCanJoinWaitingLobby() {
        UUID lobbyId = UUID.randomUUID();
        Lobby lobby = new Lobby(teamId, missionVersionId, leaderId);
        lobby.setId(lobbyId);

        LobbyMember leaderMember = new LobbyMember(lobbyId, leaderId);
        LobbyMember joiningMember = new LobbyMember(lobbyId, memberId);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).thenReturn(true);
        when(lobbyMemberRepository.existsByLobbyIdAndUserId(lobbyId, memberId)).thenReturn(false);
        when(sessionMemberRepository.existsByUserIdAndSessionStatus(memberId, MissionSessionStatus.ACTIVE)).thenReturn(false);
        when(lobbyMemberRepository.countByLobbyId(lobbyId)).thenReturn(1L);
        when(lobbyMemberRepository.findByLobbyIdOrderByJoinedAtAsc(lobbyId))
                .thenReturn(List.of(leaderMember, joiningMember));

        LobbyResponse response = lobbyService.joinLobby(lobbyId, memberId);

        assertEquals(2, response.members().size());
        verify(lobbyMemberRepository).save(any(LobbyMember.class));
    }

    @Test
    void nonTeamMemberCannotJoinLobby() {
        UUID lobbyId = UUID.randomUUID();
        UUID outsiderId = UUID.randomUUID();
        Lobby lobby = new Lobby(teamId, missionVersionId, leaderId);
        lobby.setId(lobbyId);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(teamMemberRepository.existsByTeamIdAndUserId(teamId, outsiderId)).thenReturn(false);

        SessionException exception = assertThrows(
                SessionException.class,
                () -> lobbyService.joinLobby(lobbyId, outsiderId)
        );

        assertEquals(SessionException.Reason.FORBIDDEN_ACTION, exception.getReason());
        verify(lobbyMemberRepository, never()).save(any());
    }

    @Test
    void cannotJoinWhenLobbyHasSixParticipants() {
        UUID lobbyId = UUID.randomUUID();
        Lobby lobby = new Lobby(teamId, missionVersionId, leaderId);
        lobby.setId(lobbyId);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).thenReturn(true);
        when(lobbyMemberRepository.existsByLobbyIdAndUserId(lobbyId, memberId)).thenReturn(false);
        when(sessionMemberRepository.existsByUserIdAndSessionStatus(memberId, MissionSessionStatus.ACTIVE)).thenReturn(false);
        when(lobbyMemberRepository.countByLobbyId(lobbyId)).thenReturn(6L);

        SessionException exception = assertThrows(
                SessionException.class,
                () -> lobbyService.joinLobby(lobbyId, memberId)
        );

        assertEquals(SessionException.Reason.LOBBY_FULL, exception.getReason());
        verify(lobbyMemberRepository, never()).save(any());
    }

    @Test
    void leaderCannotStartMissionWithFewerThanTwoParticipants() {
        UUID lobbyId = UUID.randomUUID();
        Lobby lobby = new Lobby(teamId, missionVersionId, leaderId);
        lobby.setId(lobbyId);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(lobbyMemberRepository.findByLobbyIdOrderByJoinedAtAsc(lobbyId))
                .thenReturn(List.of(new LobbyMember(lobbyId, leaderId)));

        SessionException exception = assertThrows(
                SessionException.class,
                () -> lobbyService.startMission(lobbyId, leaderId)
        );

        assertEquals(SessionException.Reason.NOT_ENOUGH_PARTICIPANTS, exception.getReason());
        verifyNoInteractions(missionSessionService);
    }

    @Test
    void nonLeaderCannotStartMission() {
        UUID lobbyId = UUID.randomUUID();
        Lobby lobby = new Lobby(teamId, missionVersionId, leaderId);
        lobby.setId(lobbyId);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));

        SessionException exception = assertThrows(
                SessionException.class,
                () -> lobbyService.startMission(lobbyId, memberId)
        );

        assertEquals(SessionException.Reason.FORBIDDEN_ACTION, exception.getReason());
        verifyNoInteractions(missionSessionService);
    }

    @Test
    void leaderCanStartMissionWithTwoParticipants() {
        UUID lobbyId = UUID.randomUUID();
        Lobby lobby = new Lobby(teamId, missionVersionId, leaderId);
        lobby.setId(lobbyId);

        MissionSession createdSession = new MissionSession(missionVersionId, SessionMode.TEAM, teamId, leaderId);
        createdSession.setId(UUID.randomUUID());
        createdSession.setLobbyId(lobbyId);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(lobbyMemberRepository.findByLobbyIdOrderByJoinedAtAsc(lobbyId))
                .thenReturn(List.of(
                        new LobbyMember(lobbyId, leaderId),
                        new LobbyMember(lobbyId, memberId)
                ));
        when(missionSessionService.startTeamSession(
                missionVersionId,
                teamId,
                lobbyId,
                leaderId,
                List.of(leaderId, memberId)
        )).thenReturn(createdSession);

        MissionSession result = lobbyService.startMission(lobbyId, leaderId);

        assertSame(createdSession, result);
        assertEquals(LobbyStatus.STARTED, lobby.getStatus());
        assertNotNull(lobby.getStartedAt());
        verify(lobbyRepository).save(lobby);
    }
}