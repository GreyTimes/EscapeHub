package com.escapehub.escapehub.mission.definition;

import com.escapehub.escapehub.mission.definition.dto.MissionSummaryResponse;
import com.escapehub.escapehub.mission.definition.entity.Mission;
import com.escapehub.escapehub.mission.definition.entity.MissionVersion;
import com.escapehub.escapehub.mission.definition.repository.MissionRepository;
import com.escapehub.escapehub.mission.definition.repository.MissionVersionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionVersionRepository missionVersionRepository;

    public MissionService(MissionRepository missionRepository,
                          MissionVersionRepository missionVersionRepository) {
        this.missionRepository = missionRepository;
        this.missionVersionRepository = missionVersionRepository;
    }

    public List<MissionSummaryResponse> getAvailableMissions() {
        List<Mission> missions = missionRepository.findAll();
        List<MissionSummaryResponse> result = new ArrayList<>();

        for (Mission mission : missions) {
            Optional<MissionVersion> latestVersion =
                    missionVersionRepository.findTopByMissionIdOrderByVersionNumberDesc(mission.getId());

            if (latestVersion.isPresent()) {
                MissionVersion version = latestVersion.get();
                result.add(new MissionSummaryResponse(
                        mission.getId(),
                        version.getId(),
                        mission.getTitle(),
                        mission.getDescription()
                ));
            }
        }

        return result;
    }
}