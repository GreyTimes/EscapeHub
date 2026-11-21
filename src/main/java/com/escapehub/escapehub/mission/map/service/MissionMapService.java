package com.escapehub.escapehub.mission.map.service;

import com.escapehub.escapehub.mission.map.dto.MapNodeResponse;
import com.escapehub.escapehub.mission.map.dto.MapResponse;
import com.escapehub.escapehub.mission.map.entity.MissionNode;
import com.escapehub.escapehub.mission.map.entity.NodeConnection;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.map.repository.NodeConnectionRepository;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
import com.escapehub.escapehub.mission.session.service.SessionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public MissionMapService(
            MissionSessionRepository sessionRepository,
            SessionMemberRepository memberRepository,
            MissionNodeRepository missionNodeRepository,
            NodeConnectionRepository nodeConnectionRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.memberRepository = memberRepository;
        this.missionNodeRepository = missionNodeRepository;
        this.nodeConnectionRepository = nodeConnectionRepository;
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

        return new MapResponse(
                session.getId(),
                session.getMissionVersionId(),
                member.getCurrentNodeId(),
                nodeResponses,
                connectionResponses
        );
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