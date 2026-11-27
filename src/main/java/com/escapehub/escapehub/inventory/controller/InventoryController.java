package com.escapehub.escapehub.inventory.controller;

import com.escapehub.escapehub.inventory.dto.InventoryItemResponse;
import com.escapehub.escapehub.inventory.dto.AvailableInventoryItemResponse;
import com.escapehub.escapehub.inventory.service.InventoryService;
import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{sessionId}/inventory")
    public List<InventoryItemResponse> getPlayerInventory(
            @PathVariable UUID sessionId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId
    ) {
        return inventoryService.getPlayerInventory(sessionId, currentUserId);
    }

    @GetMapping("/{sessionId}/nodes/{nodeId}/inventory-items")
    public List<AvailableInventoryItemResponse> getAvailableItemsInRoom(
            @PathVariable UUID sessionId,
            @PathVariable UUID nodeId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId
    ) {
        return inventoryService.getAvailableItemsInCurrentRoom(
                sessionId,
                currentUserId,
                nodeId
        );
    }

    @PostMapping("/{sessionId}/inventory/{itemId}/collect")
    public InventoryItemResponse collectItem(
            @PathVariable UUID sessionId,
            @PathVariable UUID itemId,
            @RequestAttribute(
                    name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE,
                    required = false
            ) UUID currentUserId
    ) {
        return inventoryService.collectItem(sessionId, currentUserId, itemId);
    }
}