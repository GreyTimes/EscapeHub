
package com.escapehub.escapehub.mission.map.service;

import com.escapehub.escapehub.mission.map.dto.MapNodeResponse;
import com.escapehub.escapehub.mission.map.dto.MapPlayerResponse;
import com.escapehub.escapehub.mission.map.dto.MapResponse;
import com.escapehub.escapehub.mission.map.entity.MissionNode;
import com.escapehub.escapehub.mission.map.entity.NodeConnection;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.map.repository.NodeConnectionRepository;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.ParticipationStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
import com.escapehub.escapehub.mission.session.service.SessionException;
import com.escapehub.escapehub.user.entity.User;
import com.escapehub.escapehub.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MissionMapService {

    private final MissionSessionRepository sessionRepository;
    private final SessionMemberRepository memberRepository;
    private final MissionNodeRepository missionNodeRepository;
    private final NodeConnectionRepository nodeConnectionRepository;
    private final UserRepository userRepository;

    public MissionMapService(
            MissionSessionRepository sessionRepository,
            SessionMemberRepository memberRepository,
            MissionNodeRepository missionNodeRepository,
            NodeConnectionRepository nodeConnectionRepository,
            UserRepository userRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.memberRepository = memberRepository;
        this.missionNodeRepository = missionNodeRepository;
        this.nodeConnectionRepository = nodeConnectionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MapResponse getMap(UUID sessionId, UUID currentUserId) {
        requireUser(currentUserId);

        MissionSession session = findSessionOrThrow(sessionId);
        SessionMember member = findMemberOrThrow(sessionId, currentUserId);

        return buildMapResponse(session, member);
    }

    @Transactional
    public MapResponse movePlayer(UUID sessionId, UUID targetNodeId, UUID currentUserId) {
        requireUser(currentUserId);

        MissionSession session = findSessionOrThrow(sessionId);

        if (!session.isActive()) {
            throw new SessionException(
                    SessionException.Reason.SESSION_NOT_ACTIVE,
                    "Cannot move in a session that is not active."
            );
        }

        SessionMember member = findMemberOrThrow(sessionId, currentUserId);

        if (member.getParticipationStatus() != ParticipationStatus.ACTIVE) {
            throw new SessionException(
                    SessionException.Reason.NOT_A_SESSION_MEMBER,
                    "You are not an active member of this session."
            );
        }

        MissionNode targetNode = missionNodeRepository.findById(targetNodeId)
                .filter(node -> node.getMissionVersionId().equals(session.getMissionVersionId()))
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.NODE_NOT_FOUND,
                        "Target room not found in this mission."
                ));

        UUID currentNodeId = member.getCurrentNodeId();
        if (currentNodeId == null) {
            throw new SessionException(
                    SessionException.Reason.NODE_NOT_CONNECTED,
                    "Player does not have a current location on this map."
            );
        }

        if (!isConnected(currentNodeId, targetNode.getId())) {
            throw new SessionException(
                    SessionException.Reason.NODE_NOT_CONNECTED,
                    "You can only move to a room connected to your current location."
            );
        }

        member.setCurrentNodeId(targetNode.getId());
        memberRepository.save(member);

        return buildMapResponse(session, member);
    }

    private boolean isConnected(UUID sourceNodeId, UUID targetNodeId) {
        return nodeConnectionRepository.existsBySourceNodeIdAndTargetNodeId(sourceNodeId, targetNodeId)
                || nodeConnectionRepository.existsBySourceNodeIdAndTargetNodeId(targetNodeId, sourceNodeId);
    }

    private MapResponse buildMapResponse(MissionSession session, SessionMember member) {
        List<MissionNode> nodes = missionNodeRepository.findByMissionVersionId(session.getMissionVersionId());
        List<UUID> nodeIds = nodes.stream()
                .map(MissionNode::getId)
                .toList();
        Set<UUID> validNodeIds = Set.copyOf(nodeIds);

        List<NodeConnection> connections = nodeIds.isEmpty()
                ? List.of()
                : nodeConnectionRepository.findBySourceNodeIdIn(nodeIds).stream()
                .filter(connection -> validNodeIds.contains(connection.getTargetNodeId()))
                .toList();

        Map<UUID, Set<UUID>> adjacency = new HashMap<>();
        for (UUID nodeId : nodeIds) {
            adjacency.put(nodeId, new LinkedHashSet<>());
        }

        for (NodeConnection connection : connections) {
            adjacency.get(connection.getSourceNodeId()).add(connection.getTargetNodeId());
            adjacency.get(connection.getTargetNodeId()).add(connection.getSourceNodeId());
        }

        List<MapNodeResponse> nodeResponses = nodes.stream()
                .map(node -> MapNodeResponse.from(
                        node,
                        List.copyOf(adjacency.getOrDefault(node.getId(), Set.of()))
                ))
                .toList();

        List<MapResponse.Connection> connectionResponses = connections.stream()
                .map(connection -> new MapResponse.Connection(
                        connection.getSourceNodeId(),
                        connection.getTargetNodeId()
                ))
                .toList();

        List<MapPlayerResponse> players = buildPlayerResponses(session.getId(), member.getUserId());

        return new MapResponse(
                session.getId(),
                session.getMissionVersionId(),
                member.getCurrentNodeId(),
                nodeResponses,
                connectionResponses,
                players
        );
    }

    private List<MapPlayerResponse> buildPlayerResponses(UUID sessionId, UUID currentUserId) {
        List<SessionMember> activeMembers = memberRepository.findBySessionId(sessionId).stream()
                .filter(sessionMember ->
                        sessionMember.getParticipationStatus() == ParticipationStatus.ACTIVE
                )
                .toList();

        if (activeMembers.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = activeMembers.stream()
                .map(SessionMember::getUserId)
                .toList();

        Map<UUID, User> usersById = new HashMap<>();
        for (User user : userRepository.findAllById(userIds)) {
            usersById.put(user.getId(), user);
        }

        List<MapPlayerResponse> players = new ArrayList<>();

        for (SessionMember sessionMember : activeMembers) {
            User user = usersById.get(sessionMember.getUserId());

            players.add(new MapPlayerResponse(
                    sessionMember.getUserId(),
                    user != null ? user.getDisplayName() : "Unknown player",
                    sessionMember.getCurrentNodeId(),
                    sessionMember.getUserId().equals(currentUserId)
            ));
        }

        return List.copyOf(players);
    }

    private MissionSession findSessionOrThrow(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.SESSION_NOT_FOUND,
                        "Session not found."
                ));
    }

    private SessionMember findMemberOrThrow(UUID sessionId, UUID userId) {
        return memberRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.NOT_A_SESSION_MEMBER,
                        "You are not a member of this session."
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
