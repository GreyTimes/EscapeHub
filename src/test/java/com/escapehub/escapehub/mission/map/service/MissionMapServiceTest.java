package com.escapehub.escapehub.mission.map.service;

import com.escapehub.escapehub.mission.map.dto.MapResponse;
import com.escapehub.escapehub.mission.map.entity.MissionNode;
import com.escapehub.escapehub.mission.map.entity.NodeConnection;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.map.repository.NodeConnectionRepository;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.entity.SessionMode;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
import com.escapehub.escapehub.mission.session.service.SessionException;
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
class MissionMapServiceTest {

    @Mock
    private MissionSessionRepository sessionRepository;
    @Mock
    private SessionMemberRepository memberRepository;
    @Mock
    private MissionNodeRepository missionNodeRepository;
    @Mock
    private NodeConnectionRepository nodeConnectionRepository;

    private MissionMapService mapService;

    private final UUID sessionId = UUID.randomUUID();
    private final UUID missionVersionId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mapService = new MissionMapService(
                sessionRepository,
                memberRepository,
                missionNodeRepository,
                nodeConnectionRepository
        );
    }

    private MissionNode createNode(UUID id, String key, String title, int x, int y, boolean starting) {
        MissionNode node = new MissionNode();
        node.setId(id);
        node.setMissionVersionId(missionVersionId);
        node.setNodeKey(key);
        node.setTitle(title);
        node.setDescription("Description for " + title);
        node.setMapX(x);
        node.setMapY(y);
        node.setStartingNode(starting);
        return node;
    }

    private NodeConnection createConnection(UUID sourceId, UUID targetId) {
        NodeConnection connection = new NodeConnection();
        connection.setId(UUID.randomUUID());
        connection.setSourceNodeId(sourceId);
        connection.setTargetNodeId(targetId);
        return connection;
    }

    @Test
    void getMapReturnsNodesConnectionsAndCurrentLocation() {
        UUID startNodeId = UUID.randomUUID();
        UUID libraryNodeId = UUID.randomUUID();

        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);

        SessionMember member = new SessionMember(sessionId, userId);
        member.setCurrentNodeId(startNodeId);

        MissionNode startNode = createNode(startNodeId, "entry", "Entry Hall", 100, 100, true);
        MissionNode libraryNode = createNode(libraryNodeId, "library", "Library", 300, 150, false);
        NodeConnection connection = createConnection(startNodeId, libraryNodeId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.of(member));
        when(missionNodeRepository.findByMissionVersionId(missionVersionId))
                .thenReturn(List.of(startNode, libraryNode));
        when(nodeConnectionRepository.findBySourceNodeIdIn(List.of(startNodeId, libraryNodeId)))
                .thenReturn(List.of(connection));

        MapResponse response = mapService.getMap(sessionId, userId);

        assertEquals(sessionId, response.sessionId());
        assertEquals(missionVersionId, response.missionVersionId());
        assertEquals(startNodeId, response.currentNodeId());
        assertEquals(2, response.nodes().size());
        assertEquals(1, response.connections().size());
        assertTrue(response.nodes().getFirst().connectedNodeIds().contains(libraryNodeId));
        assertTrue(response.nodes().getLast().connectedNodeIds().contains(startNodeId));
    }

    @Test
    void getMapRejectsNonSessionMember() {
        UUID outsiderId = UUID.randomUUID();
        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, outsiderId)).thenReturn(Optional.empty());

        SessionException exception = assertThrows(
                SessionException.class,
                () -> mapService.getMap(sessionId, outsiderId)
        );

        assertEquals(SessionException.Reason.NOT_A_SESSION_MEMBER, exception.getReason());
    }

    @Test
    void movePlayerToConnectedNodeUpdatesCurrentNodeId() {
        UUID startNodeId = UUID.randomUUID();
        UUID targetNodeId = UUID.randomUUID();

        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);

        SessionMember member = new SessionMember(sessionId, userId);
        member.setCurrentNodeId(startNodeId);

        MissionNode startNode = createNode(startNodeId, "entry", "Entry Hall", 100, 100, true);
        MissionNode targetNode = createNode(targetNodeId, "library", "Library", 300, 150, false);
        NodeConnection connection = createConnection(startNodeId, targetNodeId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.of(member));
        when(missionNodeRepository.findById(targetNodeId)).thenReturn(Optional.of(targetNode));
        when(nodeConnectionRepository.existsBySourceNodeIdAndTargetNodeId(startNodeId, targetNodeId))
                .thenReturn(true);
        when(missionNodeRepository.findByMissionVersionId(missionVersionId))
                .thenReturn(List.of(startNode, targetNode));
        when(nodeConnectionRepository.findBySourceNodeIdIn(List.of(startNodeId, targetNodeId)))
                .thenReturn(List.of(connection));

        MapResponse response = mapService.movePlayer(sessionId, targetNodeId, userId);

        assertEquals(targetNodeId, member.getCurrentNodeId());
        assertEquals(targetNodeId, response.currentNodeId());
        verify(memberRepository).save(member);
    }

    @Test
    void movePlayerToReverseConnectedNodeUpdatesCurrentNodeId() {
        UUID startNodeId = UUID.randomUUID();
        UUID libraryNodeId = UUID.randomUUID();

        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);

        SessionMember member = new SessionMember(sessionId, userId);
        member.setCurrentNodeId(libraryNodeId);

        MissionNode startNode = createNode(startNodeId, "entry", "Entry Hall", 100, 100, true);
        MissionNode libraryNode = createNode(libraryNodeId, "library", "Library", 300, 150, false);
        NodeConnection connection = createConnection(startNodeId, libraryNodeId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.of(member));
        when(missionNodeRepository.findById(startNodeId)).thenReturn(Optional.of(startNode));
        when(nodeConnectionRepository.existsBySourceNodeIdAndTargetNodeId(libraryNodeId, startNodeId))
                .thenReturn(false);
        when(nodeConnectionRepository.existsBySourceNodeIdAndTargetNodeId(startNodeId, libraryNodeId))
                .thenReturn(true);
        when(missionNodeRepository.findByMissionVersionId(missionVersionId))
                .thenReturn(List.of(startNode, libraryNode));
        when(nodeConnectionRepository.findBySourceNodeIdIn(List.of(startNodeId, libraryNodeId)))
                .thenReturn(List.of(connection));

        MapResponse response = mapService.movePlayer(sessionId, startNodeId, userId);

        assertEquals(startNodeId, member.getCurrentNodeId());
        assertEquals(startNodeId, response.currentNodeId());
        verify(memberRepository).save(member);
    }

    @Test
    void movePlayerToDisconnectedNodeIsRejected() {
        UUID startNodeId = UUID.randomUUID();
        UUID farNodeId = UUID.randomUUID();

        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);

        SessionMember member = new SessionMember(sessionId, userId);
        member.setCurrentNodeId(startNodeId);

        MissionNode farNode = createNode(farNodeId, "vault", "Secret Vault", 500, 400, false);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.of(member));
        when(missionNodeRepository.findById(farNodeId)).thenReturn(Optional.of(farNode));
        when(nodeConnectionRepository.existsBySourceNodeIdAndTargetNodeId(startNodeId, farNodeId))
                .thenReturn(false);
        when(nodeConnectionRepository.existsBySourceNodeIdAndTargetNodeId(farNodeId, startNodeId))
                .thenReturn(false);

        SessionException exception = assertThrows(
                SessionException.class,
                () -> mapService.movePlayer(sessionId, farNodeId, userId)
        );

        assertEquals(SessionException.Reason.NODE_NOT_CONNECTED, exception.getReason());
        assertEquals(startNodeId, member.getCurrentNodeId());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void movePlayerToNodeOfDifferentMissionVersionIsRejected() {
        UUID startNodeId = UUID.randomUUID();
        UUID otherMissionNodeId = UUID.randomUUID();

        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);

        SessionMember member = new SessionMember(sessionId, userId);
        member.setCurrentNodeId(startNodeId);

        MissionNode otherMissionNode = new MissionNode();
        otherMissionNode.setId(otherMissionNodeId);
        otherMissionNode.setMissionVersionId(UUID.randomUUID());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.of(member));
        when(missionNodeRepository.findById(otherMissionNodeId)).thenReturn(Optional.of(otherMissionNode));

        SessionException exception = assertThrows(
                SessionException.class,
                () -> mapService.movePlayer(sessionId, otherMissionNodeId, userId)
        );

        assertEquals(SessionException.Reason.NODE_NOT_FOUND, exception.getReason());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void movePlayerInInactiveSessionIsRejected() {
        UUID targetNodeId = UUID.randomUUID();
        MissionSession session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);
        session.setStatus(MissionSessionStatus.ABANDONED);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        SessionException exception = assertThrows(
                SessionException.class,
                () -> mapService.movePlayer(sessionId, targetNodeId, userId)
        );

        assertEquals(SessionException.Reason.SESSION_NOT_ACTIVE, exception.getReason());
        verify(memberRepository, never()).save(any());
    }
}