package com.escapehub.escapehub.mission.map.controller;

import com.escapehub.escapehub.config.DevelopmentCurrentUserFilter;
import com.escapehub.escapehub.mission.map.dto.MapResponse;
import com.escapehub.escapehub.mission.map.dto.MovePlayerRequest;
import com.escapehub.escapehub.mission.map.service.MissionMapService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MissionMapController {

    private final MissionMapService missionMapService;

    public MissionMapController(MissionMapService missionMapService) {
        this.missionMapService = missionMapService;
    }

    @GetMapping("/sessions/{sessionId}/map")
    public MapResponse getMap(
            @PathVariable UUID sessionId,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        return missionMapService.getMap(sessionId, currentUserId);
    }

    @PatchMapping("/sessions/{sessionId}/location")
    public MapResponse movePlayer(
            @PathVariable UUID sessionId,
            @Valid @RequestBody MovePlayerRequest request,
            @RequestAttribute(name = DevelopmentCurrentUserFilter.CURRENT_USER_ID_ATTRIBUTE, required = false)
            UUID currentUserId
    ) {
        return missionMapService.movePlayer(sessionId, request.targetNodeId(), currentUserId);
    }
}