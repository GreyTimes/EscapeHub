package com.escapehub.escapehub.inventory.service;

import com.escapehub.escapehub.inventory.dto.AvailableInventoryItemResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final PlayerInventoryItemRepository playerInventoryItemRepository;
    private final SessionMemberRepository sessionMemberRepository;
    private final MissionSessionRepository missionSessionRepository;
    private final MissionNodeRepository missionNodeRepository;

    public InventoryService(
            InventoryItemRepository inventoryItemRepository,
            PlayerInventoryItemRepository playerInventoryItemRepository,
            SessionMemberRepository sessionMemberRepository,
            MissionSessionRepository missionSessionRepository,
            MissionNodeRepository missionNodeRepository
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.playerInventoryItemRepository = playerInventoryItemRepository;
        this.sessionMemberRepository = sessionMemberRepository;
        this.missionSessionRepository = missionSessionRepository;
        this.missionNodeRepository = missionNodeRepository;
    }
    private SessionMember getActiveSessionMember(UUID sessionId, UUID userId) {

        SessionMember member = sessionMemberRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this session"));

        if (member.getParticipationStatus() != ParticipationStatus.ACTIVE) {
            throw new RuntimeException("User is not an active member of this session");
        }

        return member;
    }
    private MissionSession getActiveSession(UUID sessionId) {

        MissionSession session = missionSessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.isActive()) {
            throw new RuntimeException("Session is not active");
        }

        return session;
    }
    private InventoryItem getItemFromSessionMission(
            UUID itemId,
            UUID missionVersionId
    ) {

        InventoryItem item = inventoryItemRepository
                .findById(itemId)
                .orElseThrow(() -> new RuntimeException("Inventory item not found"));

        MissionNode node = missionNodeRepository
                .findById(item.getNodeId())
                .orElseThrow(() -> new RuntimeException("Mission node not found"));

        if (!node.getMissionVersionId().equals(missionVersionId)) {
            throw new RuntimeException("Item does not belong to this mission");
        }

        return item;
    }
    private void validatePlayerIsInItemRoom(
            SessionMember member,
            InventoryItem item
    ) {

        if (member.getCurrentNodeId() == null
                || !member.getCurrentNodeId().equals(item.getNodeId())) {
            throw new RuntimeException("Player is not in the room where this item is located");
        }
    }
    private void validateInventoryCapacity(UUID sessionMemberId) {

        long currentItemCount = playerInventoryItemRepository
                .countBySessionMemberId(sessionMemberId);

        if (currentItemCount >= 5) {
            throw new RuntimeException("Inventory is full (maximum 5 items)");
        }
    }
    private void validateItemNotAlreadyCollected(
            UUID sessionMemberId,
            UUID inventoryItemId
    ) {

        boolean alreadyCollected = playerInventoryItemRepository
                .existsBySessionMemberIdAndInventoryItemId(
                        sessionMemberId,
                        inventoryItemId
                );

        if (alreadyCollected) {
            throw new RuntimeException("Item has already been collected");
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getPlayerInventory(
            UUID sessionId,
            UUID userId
    ) {

        getActiveSession(sessionId);

        SessionMember member = getActiveSessionMember(sessionId, userId);

        List<PlayerInventoryItem> collectedItems =
                playerInventoryItemRepository.findBySessionMemberId(member.getId());

        return collectedItems.stream()
                .map(playerInventoryItem -> {

                    InventoryItem item = inventoryItemRepository
                            .findById(playerInventoryItem.getInventoryItemId())
                            .orElseThrow(() -> new RuntimeException("Inventory item not found"));

                    return new InventoryItemResponse(
                            item.getId(),
                            item.getItemKey(),
                            item.getTitle(),
                            item.getDescription(),
                            playerInventoryItem.getCollectedAt()
                    );
                })
                .toList();
    }
    @Transactional(readOnly = true)
    public List<AvailableInventoryItemResponse> getAvailableItemsInCurrentRoom(
            UUID sessionId,
            UUID userId,
            UUID nodeId
    ) {

        MissionSession session = getActiveSession(sessionId);

        SessionMember member = getActiveSessionMember(sessionId, userId);

        if (member.getCurrentNodeId() == null
                || !member.getCurrentNodeId().equals(nodeId)) {
            throw new RuntimeException("Player is not in this room");
        }

        MissionNode node = missionNodeRepository
                .findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Mission node not found"));

        if (!node.getMissionVersionId().equals(session.getMissionVersionId())) {
            throw new RuntimeException("Node does not belong to this mission");
        }

        List<InventoryItem> items = inventoryItemRepository.findByNodeId(nodeId);

        return items.stream()
                .map(item -> {

                    boolean collected = playerInventoryItemRepository
                            .existsBySessionMemberIdAndInventoryItemId(
                                    member.getId(),
                                    item.getId()
                            );

                    return new AvailableInventoryItemResponse(
                            item.getId(),
                            item.getItemKey(),
                            item.getTitle(),
                            item.getDescription(),
                            collected
                    );
                })
                .toList();
    }
    @Transactional
    public InventoryItemResponse collectItem(
            UUID sessionId,
            UUID userId,
            UUID itemId
    ) {

        MissionSession session = getActiveSession(sessionId);

        SessionMember member = getActiveSessionMember(sessionId, userId);

        InventoryItem item = getItemFromSessionMission(
                itemId,
                session.getMissionVersionId()
        );

        validatePlayerIsInItemRoom(member, item);

        validateItemNotAlreadyCollected(member.getId(), item.getId());

        validateInventoryCapacity(member.getId());

        PlayerInventoryItem playerInventoryItem =
                new PlayerInventoryItem(member.getId(), item.getId());

        playerInventoryItemRepository.save(playerInventoryItem);

        return new InventoryItemResponse(
                item.getId(),
                item.getItemKey(),
                item.getTitle(),
                item.getDescription(),
                playerInventoryItem.getCollectedAt()
        );
    }
}