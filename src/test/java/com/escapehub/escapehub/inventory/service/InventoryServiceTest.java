package com.escapehub.escapehub.inventory.service;

import com.escapehub.escapehub.inventory.dto.InventoryItemResponse;
import com.escapehub.escapehub.inventory.entity.InventoryItem;
import com.escapehub.escapehub.inventory.entity.PlayerInventoryItem;
import com.escapehub.escapehub.inventory.repository.InventoryItemRepository;
import com.escapehub.escapehub.inventory.repository.PlayerInventoryItemRepository;
import com.escapehub.escapehub.mission.map.entity.MissionNode;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.ParticipationStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private PlayerInventoryItemRepository playerInventoryItemRepository;

    @Mock
    private SessionMemberRepository sessionMemberRepository;

    @Mock
    private MissionSessionRepository missionSessionRepository;

    @Mock
    private MissionNodeRepository missionNodeRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void collectItemSuccessfully() {

        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionMemberId = UUID.randomUUID();
        UUID missionVersionId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        MissionSession session = mock(MissionSession.class);
        when(session.isActive()).thenReturn(true);
        when(session.getMissionVersionId()).thenReturn(missionVersionId);

        SessionMember member = mock(SessionMember.class);
        when(member.getId()).thenReturn(sessionMemberId);
        when(member.getParticipationStatus()).thenReturn(ParticipationStatus.ACTIVE);
        when(member.getCurrentNodeId()).thenReturn(nodeId);

        InventoryItem item = new InventoryItem();
        item.setId(itemId);
        item.setNodeId(nodeId);
        item.setItemKey("key");
        item.setTitle("Key");
        item.setDescription("A small key");

        MissionNode node = mock(MissionNode.class);
        when(node.getMissionVersionId()).thenReturn(missionVersionId);

        when(missionSessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(sessionMemberRepository.findBySessionIdAndUserId(sessionId, userId))
                .thenReturn(Optional.of(member));

        when(inventoryItemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        when(missionNodeRepository.findById(nodeId))
                .thenReturn(Optional.of(node));

        when(playerInventoryItemRepository.existsBySessionMemberIdAndInventoryItemId(
                sessionMemberId, itemId
        )).thenReturn(false);

        when(playerInventoryItemRepository.countBySessionMemberId(sessionMemberId))
                .thenReturn(0L);

        InventoryItemResponse response =
                inventoryService.collectItem(sessionId, userId, itemId);

        assertNotNull(response);
        assertEquals(itemId, response.getId());
        assertEquals("Key", response.getTitle());

        verify(playerInventoryItemRepository).save(any(PlayerInventoryItem.class));
    }
}