package com.escapehub.escapehub.mission.session.service;

import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.entity.ParticipationStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.entity.SessionMode;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionSessionServiceTest {

    @Mock
    private MissionSessionRepository sessionRepository;

    @Mock
    private SessionMemberRepository memberRepository;

    private MissionSessionService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID missionVersionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new MissionSessionService(sessionRepository, memberRepository);
    }

    @Test
    void soloStartCreatesActiveSessionWithOneMember() {
        when(memberRepository.existsByUserIdAndSessionStatus(userId, MissionSessionStatus.ACTIVE))
                .thenReturn(false);
        when(sessionRepository.save(any(MissionSession.class))).thenAnswer(invocation -> {
            MissionSession saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        MissionSession session = service.startSession(SessionMode.SOLO, missionVersionId, userId);

        assertNotNull(session.getId());
        assertEquals(SessionMode.SOLO, session.getMode());
        assertEquals(MissionSessionStatus.ACTIVE, session.getStatus());
        assertEquals(missionVersionId, session.getMissionVersionId());
        assertEquals(userId, session.getStartedByUserId());
        assertNull(session.getTeamId());
        assertNull(session.getLobbyId());

        ArgumentCaptor<SessionMember> memberCaptor = ArgumentCaptor.forClass(SessionMember.class);
        verify(memberRepository, times(1)).save(memberCaptor.capture());

        SessionMember member = memberCaptor.getValue();
        assertEquals(session.getId(), member.getSessionId());
        assertEquals(userId, member.getUserId());
        assertEquals(ParticipationStatus.ACTIVE, member.getParticipationStatus());
    }

    @Test
    void soloStartIsRejectedWhenUserAlreadyHasActiveSession() {
        when(memberRepository.existsByUserIdAndSessionStatus(userId, MissionSessionStatus.ACTIVE))
                .thenReturn(true);

        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.startSession(SessionMode.SOLO, missionVersionId, userId)
        );

        assertEquals(SessionException.Reason.ACTIVE_SESSION_EXISTS, exception.getReason());
        verify(sessionRepository, never()).save(any());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void teamModeIsRejectedBecauseItStartsFromLobby() {
        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.startSession(SessionMode.TEAM, missionVersionId, userId)
        );

        assertEquals(SessionException.Reason.TEAM_MODE_REQUIRES_LOBBY, exception.getReason());
        verifyNoInteractions(sessionRepository);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void startWithoutCurrentUserIsRejected() {
        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.startSession(SessionMode.SOLO, missionVersionId, null)
        );

        assertEquals(SessionException.Reason.MISSING_CURRENT_USER, exception.getReason());
        verifyNoInteractions(sessionRepository, memberRepository);
    }

    @Test
    void memberCanViewTheirSession() {
        UUID sessionId = UUID.randomUUID();
        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.existsBySessionIdAndUserId(sessionId, userId)).thenReturn(true);

        assertSame(session, service.getSessionForUser(sessionId, userId));
    }

    @Test
    void nonMemberCannotViewSession() {
        UUID sessionId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.existsBySessionIdAndUserId(sessionId, otherUserId)).thenReturn(false);

        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.getSessionForUser(sessionId, otherUserId)
        );

        assertEquals(SessionException.Reason.NOT_A_SESSION_MEMBER, exception.getReason());
    }

    @Test
    void unknownSessionIsReportedAsNotFound() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.getSessionForUser(sessionId, userId)
        );

        assertEquals(SessionException.Reason.SESSION_NOT_FOUND, exception.getReason());
    }

    @Test
    void soloSessionCannotHaveTeamAndTeamSessionNeedsTeam() {
        UUID teamId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new MissionSession(missionVersionId, SessionMode.SOLO, teamId, userId)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new MissionSession(missionVersionId, SessionMode.TEAM, null, userId)
        );
        assertDoesNotThrow(
                () -> new MissionSession(missionVersionId, SessionMode.TEAM, teamId, userId)
        );
    }
}
