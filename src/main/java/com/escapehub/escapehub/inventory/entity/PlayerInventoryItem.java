package com.escapehub.escapehub.inventory.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "player_inventory_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"session_member_id", "inventory_item_id"}
                )
        }
)
public class PlayerInventoryItem {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "session_member_id", nullable = false)
    private UUID sessionMemberId;

    @Column(name = "inventory_item_id", nullable = false)
    private UUID inventoryItemId;

    @Column(nullable = false)
    private Instant collectedAt;

    public PlayerInventoryItem() {
    }

    public PlayerInventoryItem(UUID sessionMemberId, UUID inventoryItemId) {
        this.sessionMemberId = sessionMemberId;
        this.inventoryItemId = inventoryItemId;
        this.collectedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (collectedAt == null) {
            collectedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionMemberId() {
        return sessionMemberId;
    }

    public void setSessionMemberId(UUID sessionMemberId) {
        this.sessionMemberId = sessionMemberId;
    }

    public UUID getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(UUID inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }
}