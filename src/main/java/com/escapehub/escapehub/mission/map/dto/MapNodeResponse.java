package com.escapehub.escapehub.mission.map.dto;

import com.escapehub.escapehub.mission.map.entity.MissionNode;

import java.util.List;
import java.util.UUID;

public record MapNodeResponse(
        UUID id,
        String nodeKey,
        String title,
        String description,
        int mapX,
        int mapY,
        boolean startingNode,
        List<UUID> connectedNodeIds
) {
    public static MapNodeResponse from(MissionNode node, List<UUID> connectedNodeIds) {
        return new MapNodeResponse(
                node.getId(),
                node.getNodeKey(),
                node.getTitle(),
                node.getDescription(),
                node.getMapX(),
                node.getMapY(),
                node.isStartingNode(),
                connectedNodeIds
        );
    }
}