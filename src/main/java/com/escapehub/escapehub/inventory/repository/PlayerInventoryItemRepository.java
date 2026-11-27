package com.escapehub.escapehub.inventory.repository;

import com.escapehub.escapehub.inventory.entity.PlayerInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerInventoryItemRepository
        extends JpaRepository<PlayerInventoryItem, UUID> {

    List<PlayerInventoryItem> findBySessionMemberId(UUID sessionMemberId);

    boolean existsBySessionMemberIdAndInventoryItemId(
            UUID sessionMemberId,
            UUID inventoryItemId
    );

    long countBySessionMemberId(UUID sessionMemberId);
}