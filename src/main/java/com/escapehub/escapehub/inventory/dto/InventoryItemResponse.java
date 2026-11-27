package com.escapehub.escapehub.inventory.dto;

import java.time.Instant;
import java.util.UUID;

public class InventoryItemResponse {

    private UUID id;
    private String itemKey;
    private String title;
    private String description;
    private Instant collectedAt;

    public InventoryItemResponse() {
    }

    public InventoryItemResponse(
            UUID id,
            String itemKey,
            String title,
            String description,
            Instant collectedAt
    ) {
        this.id = id;
        this.itemKey = itemKey;
        this.title = title;
        this.description = description;
        this.collectedAt = collectedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getItemKey() {
        return itemKey;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }
}