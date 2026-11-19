package com.escapehub.escapehub.mission.session.service;

import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.entity.SessionMode;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Starts mission sessions based on the mode the player chose.
 *
 * SOLO: a session with one member is created directly, without a lobby.
 * TEAM: team sessions are created only from a team lobby
 *       (LobbyService.startMission, architecture section 10.4),
 *       so this service rejects TEAM requests with a clear message.
 */
@Service
public class MissionSessionService {

    private final MissionSessionRepository sessionRepository;
    private final SessionMemberRepository memberRepository;

    public MissionSessionService(
            MissionSessionRepository sessionRepository,
            SessionMemberRepository memberRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MissionSession startSession(SessionMode mode, UUID missionVersionId, UUID userId) {
        requireUser(userId);

        if (mode == SessionMode.TEAM) {
            throw new SessionException(
                    SessionException.Reason.TEAM_MODE_REQUIRES_LOBBY,
                    "Team missions are started by the team leader from a team lobby."
            );
        }

        return startSoloSession(missionVersionId, userId);
    }

    private MissionSession startSoloSession(UUID missionVersionId, UUID userId) {
        // A user cannot take part in two active missions at the same time.
        if (memberRepository.existsByUserIdAndSessionStatus(userId, MissionSessionStatus.ACTIVE)) {
            throw new SessionException(
                    SessionException.Reason.ACTIVE_SESSION_EXISTS,
                    "You already have an active mission. Finish or abandon it before starting a new one."
            );
        }

        MissionSession session = sessionRepository.save(
                new MissionSession(missionVersionId, SessionMode.SOLO, null, userId)
        );

        memberRepository.save(new SessionMember(session.getId(), userId));

        return session;
    }

    @Transactional(readOnly = true)
    public MissionSession getSessionForUser(UUID sessionId, UUID userId) {
        requireUser(userId);

        MissionSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.SESSION_NOT_FOUND,
                        "Session not found."
                ));

        // Only players of the session may see it.
        if (!memberRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            throw new SessionException(
                    SessionException.Reason.NOT_A_SESSION_MEMBER,
                    "You are not a member of this session."
            );
        }

        return session;
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
