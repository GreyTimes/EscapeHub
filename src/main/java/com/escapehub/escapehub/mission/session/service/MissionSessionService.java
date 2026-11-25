package com.escapehub.escapehub.mission.session.service;

import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.entity.SessionMode;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.escapehub.escapehub.mission.map.entity.MissionNode;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.session.entity.ParticipationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MissionSessionService {
    private final MissionSessionRepository sessionRepository;
    private final SessionMemberRepository memberRepository;
    private final MissionNodeRepository missionNodeRepository;

    public MissionSessionService(
            MissionSessionRepository sessionRepository,
            SessionMemberRepository memberRepository,
            MissionNodeRepository missionNodeRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.memberRepository = memberRepository;
        this.missionNodeRepository = missionNodeRepository;
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
        if (memberRepository.existsByUserIdAndSessionStatus(userId, MissionSessionStatus.ACTIVE)) {
            throw new SessionException(
                    SessionException.Reason.ACTIVE_SESSION_EXISTS,
                    "You already have an active mission. Finish or abandon it before starting a new one."
            );
        }

        MissionSession session = sessionRepository.save(
                new MissionSession(missionVersionId, SessionMode.SOLO, null, userId)
        );

        UUID startingNodeId = findStartingNodeId(missionVersionId);
        SessionMember member = new SessionMember(session.getId(), userId);
        member.setCurrentNodeId(startingNodeId);
        memberRepository.save(member);

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

        if (!memberRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            throw new SessionException(
                    SessionException.Reason.NOT_A_SESSION_MEMBER,
                    "You are not a member of this session."
            );
        }

        return session;
    }

    @Transactional(readOnly = true)
    public Optional<MissionSession> getActiveSessionForUser(UUID userId) {
        requireUser(userId);

        return memberRepository.findActiveSessionsForUser(
                userId,
                ParticipationStatus.ACTIVE,
                MissionSessionStatus.ACTIVE
        ).stream().findFirst();
    }

    @Transactional
    public MissionSession startTeamSession(
            UUID missionVersionId,
            UUID teamId,
            UUID lobbyId,
            UUID leaderUserId,
            List<UUID> participantUserIds
    ) {
        requireUser(leaderUserId);

        if (sessionRepository.existsByTeamIdAndStatus(teamId, MissionSessionStatus.ACTIVE)) {
            throw new SessionException(
                    SessionException.Reason.ACTIVE_SESSION_EXISTS,
                    "This team already has an active mission."
            );
        }

        for (UUID participantId : participantUserIds) {
            if (memberRepository.existsByUserIdAndSessionStatus(participantId, MissionSessionStatus.ACTIVE)) {
                throw new SessionException(
                        SessionException.Reason.ACTIVE_SESSION_EXISTS,
                        "One of the lobby members already participates in an active mission."
                );
            }
        }

        MissionSession session = new MissionSession(missionVersionId, SessionMode.TEAM, teamId, leaderUserId);
        session.setLobbyId(lobbyId);
        session = sessionRepository.save(session);

        UUID startingNodeId = findStartingNodeId(missionVersionId);
        for (UUID participantId : participantUserIds) {
            SessionMember member = new SessionMember(session.getId(), participantId);
            member.setCurrentNodeId(startingNodeId);
            memberRepository.save(member);
        }

        return session;
    }

    @Transactional
    public MissionSession abandonSession(UUID sessionId, UUID userId) {
        requireUser(userId);

        MissionSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.SESSION_NOT_FOUND,
                        "Session not found."
                ));

        if (!session.isActive()) {
            throw new SessionException(
                    SessionException.Reason.SESSION_NOT_ACTIVE,
                    "Only an active session can be abandoned."
            );
        }

        if (!session.getStartedByUserId().equals(userId)) {
            throw new SessionException(
                    SessionException.Reason.FORBIDDEN_ACTION,
                    "Only the session owner or team leader can abandon this session."
            );
        }

        session.setStatus(MissionSessionStatus.ABANDONED);
        session.setAbandonedAt(Instant.now());
        sessionRepository.save(session);

        List<SessionMember> members = memberRepository.findBySessionId(sessionId);
        for (SessionMember member : members) {
            member.setParticipationStatus(ParticipationStatus.FINISHED);
        }
        memberRepository.saveAll(members);

        return session;
    }

    @Transactional
    public MissionSession completeSession(MissionSession session) {
        if (!session.isActive()) {
            throw new SessionException(
                    SessionException.Reason.SESSION_NOT_ACTIVE,
                    "Only an active session can be completed."
            );
        }

        session.setStatus(MissionSessionStatus.COMPLETED);
        session.setCompletedAt(Instant.now());
        sessionRepository.save(session);

        List<SessionMember> members = memberRepository.findBySessionId(session.getId());
        for (SessionMember member : members) {
            member.setParticipationStatus(ParticipationStatus.FINISHED);
        }
        memberRepository.saveAll(members);

        return session;
    }

    private UUID findStartingNodeId(UUID missionVersionId) {
        return missionNodeRepository.findByMissionVersionId(missionVersionId).stream()
                .filter(MissionNode::isStartingNode)
                .findFirst()
                .map(MissionNode::getId)
                .orElse(null);
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
