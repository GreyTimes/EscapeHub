package com.escapehub.escapehub.mission.definition;

import com.escapehub.escapehub.mission.definition.dto.MissionSummaryResponse;
import com.escapehub.escapehub.mission.definition.entity.Mission;
import com.escapehub.escapehub.mission.definition.entity.MissionVersion;
import com.escapehub.escapehub.mission.definition.repository.MissionRepository;
import com.escapehub.escapehub.mission.definition.repository.MissionVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private MissionVersionRepository missionVersionRepository;

    @InjectMocks
    private MissionService missionService;

    @Test
    void getAvailableMissions_returnsMissionsWithLatestVersion() {
        UUID missionId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        Mission mission = new Mission();
        mission.setId(missionId);
        mission.setTitle("The Locked Lab");
        mission.setDescription("Escape the abandoned lab.");

        MissionVersion version = new MissionVersion();
        version.setId(versionId);
        version.setMission(mission);
        version.setVersionNumber(2);

        when(missionRepository.findAll()).thenReturn(List.of(mission));
        when(missionVersionRepository.findTopByMissionIdOrderByVersionNumberDesc(missionId))
                .thenReturn(Optional.of(version));

        List<MissionSummaryResponse> result = missionService.getAvailableMissions();

        assertEquals(1, result.size());
        assertEquals(missionId, result.get(0).missionId());
        assertEquals(versionId, result.get(0).missionVersionId());
        assertEquals("The Locked Lab", result.get(0).title());
        assertEquals("Escape the abandoned lab.", result.get(0).description());
    }

    @Test
    void getAvailableMissions_skipsMissionsWithoutVersion() {
        UUID missionId = UUID.randomUUID();

        Mission mission = new Mission();
        mission.setId(missionId);
        mission.setTitle("Draft Mission");
        mission.setDescription("No versions yet.");

        when(missionRepository.findAll()).thenReturn(List.of(mission));
        when(missionVersionRepository.findTopByMissionIdOrderByVersionNumberDesc(missionId))
                .thenReturn(Optional.empty());

        List<MissionSummaryResponse> result = missionService.getAvailableMissions();

        assertTrue(result.isEmpty());
    }
}