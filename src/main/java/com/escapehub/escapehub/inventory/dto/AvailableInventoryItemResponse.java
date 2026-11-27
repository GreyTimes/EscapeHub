package com.escapehub.escapehub.inventory.dto;

import java.util.UUID;

public class AvailableInventoryItemResponse {

    private UUID id;
    private String itemKey;
    private String title;
    private String description;
    private boolean collected;

    public AvailableInventoryItemResponse() {
    }

    public AvailableInventoryItemResponse(
            UUID id,
            String itemKey,
            String title,
            String description,
            boolean collected
    ) {
        this.id = id;
        this.itemKey = itemKey;
        this.title = title;
        this.description = description;
        this.collected = collected;
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

    public boolean isCollected() {
        return collected;
    }
}