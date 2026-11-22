package com.escapehub.escapehub.mission.definition;

import com.escapehub.escapehub.mission.definition.dto.MissionSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    public List<MissionSummaryResponse> getAvailableMissions() {
        return missionService.getAvailableMissions();
    }
}