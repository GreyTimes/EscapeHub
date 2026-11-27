package com.escapehub.escapehub.mission.definition.importer;

import com.escapehub.escapehub.evidence.repository.EvidenceRepository;
import com.escapehub.escapehub.inventory.repository.InventoryItemRepository;
import com.escapehub.escapehub.mission.definition.entity.Mission;
import com.escapehub.escapehub.mission.definition.entity.MissionVersion;
import com.escapehub.escapehub.mission.definition.entity.NodeUnlockCondition;
import com.escapehub.escapehub.mission.definition.entity.NodeUnlockRule;
import com.escapehub.escapehub.mission.definition.entity.UnlockConditionType;
import com.escapehub.escapehub.mission.definition.entity.UnlockOperator;
import com.escapehub.escapehub.mission.definition.repository.MissionRepository;
import com.escapehub.escapehub.mission.definition.repository.MissionVersionRepository;
import com.escapehub.escapehub.mission.definition.repository.NodeUnlockConditionRepository;
import com.escapehub.escapehub.mission.definition.repository.NodeUnlockRuleRepository;
import com.escapehub.escapehub.mission.map.entity.MissionNode;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.map.repository.NodeConnectionRepository;
import com.escapehub.escapehub.puzzle.repository.PuzzleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionImportServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private MissionVersionRepository missionVersionRepository;

    @Mock
    private MissionNodeRepository missionNodeRepository;

    @Mock
    private NodeConnectionRepository nodeConnectionRepository;

    @Mock
    private PuzzleRepository puzzleRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private NodeUnlockRuleRepository nodeUnlockRuleRepository;

    @Mock
    private NodeUnlockConditionRepository nodeUnlockConditionRepository;

    private MissionImportService importService;

    private final UUID missionId = UUID.randomUUID();
    private final UUID versionId = UUID.randomUUID();

    private Mission mission;

    @BeforeEach
    void setUp() {
        importService = new MissionImportService(
                missionRepository,
                missionVersionRepository,
                missionNodeRepository,
                nodeConnectionRepository,
                puzzleRepository,
                evidenceRepository,
                inventoryItemRepository,
                nodeUnlockRuleRepository,
                nodeUnlockConditionRepository,
                new MissionDefinitionValidator()
        );

        mission = new Mission();
        mission.setId(missionId);
        mission.setMissionKey("sample");
        mission.setTitle("Sample");
    }

    @Test
    void importMission_persistsUnlockRulesAndConditionsForTheMissionVersion() {
        String sourceJson = "{\"mission\":\"sample\",\"version\":2}";

        MissionDefinitionDocument definition = validDefinition();

        when(missionRepository.findByMissionKey("sample"))
                .thenReturn(Optional.of(mission));

        when(missionVersionRepository.findByMissionIdAndVersionNumber(missionId, 2))
                .thenReturn(Optional.empty());

        when(missionVersionRepository.saveAndFlush(any(MissionVersion.class)))
                .thenAnswer(invocation -> {
                    MissionVersion version = invocation.getArgument(0);
                    version.setId(versionId);
                    return version;
                });

        when(missionNodeRepository.save(any(MissionNode.class)))
                .thenAnswer(invocation -> {
                    MissionNode node = invocation.getArgument(0);
                    node.setId(UUID.randomUUID());
                    return node;
                });

        when(nodeUnlockRuleRepository.saveAndFlush(any(NodeUnlockRule.class)))
                .thenAnswer(invocation -> {
                    NodeUnlockRule rule = invocation.getArgument(0);
                    rule.setId(UUID.randomUUID());
                    return rule;
                });

        List<NodeUnlockCondition> savedConditions = new ArrayList<>();

        doAnswer(invocation -> {
            Iterable<NodeUnlockCondition> conditions = invocation.getArgument(0);
            conditions.forEach(savedConditions::add);
            return savedConditions;
        }).when(nodeUnlockConditionRepository).saveAll(any());

        MissionVersion importedVersion = importService.importMission(
                definition,
                sourceJson
        );

        assertEquals(versionId, importedVersion.getId());

        ArgumentCaptor<NodeUnlockRule> ruleCaptor =
                ArgumentCaptor.forClass(NodeUnlockRule.class);

        verify(nodeUnlockRuleRepository, times(2))
                .saveAndFlush(ruleCaptor.capture());

        List<NodeUnlockRule> savedRules = ruleCaptor.getAllValues();

        assertEquals(2, savedRules.size());

        assertEquals(
                versionId,
                savedRules.get(0).getMissionVersionId()
        );

        assertEquals(
                UnlockOperator.AND,
                savedRules.get(0).getOperator()
        );

        assertEquals(
                UnlockOperator.OR,
                savedRules.get(1).getOperator()
        );

        assertEquals(
                savedRules.get(0).getTargetNodeId(),
                savedRules.get(1).getTargetNodeId()
        );

        assertEquals(3, savedConditions.size());

        assertEquals(
                UnlockConditionType.EVIDENCE_DISCOVERED,
                savedConditions.get(0).getConditionType()
        );

        assertEquals(
                "catalogue-card",
                savedConditions.get(0).getReferenceKey()
        );

        assertEquals(
                UnlockConditionType.EVIDENCE_DISCOVERED,
                savedConditions.get(1).getConditionType()
        );

        assertEquals(
                "signed-note",
                savedConditions.get(1).getReferenceKey()
        );

        assertEquals(
                UnlockConditionType.PUZZLE_SOLVED,
                savedConditions.get(2).getConditionType()
        );

        assertEquals(
                "archive-code",
                savedConditions.get(2).getReferenceKey()
        );

        assertEquals(
                savedRules.get(0).getId(),
                savedConditions.get(0).getNodeUnlockRuleId()
        );

        assertEquals(
                savedRules.get(0).getId(),
                savedConditions.get(1).getNodeUnlockRuleId()
        );

        assertEquals(
                savedRules.get(1).getId(),
                savedConditions.get(2).getNodeUnlockRuleId()
        );
    }

    @Test
    void importMission_sameVersionAndSameJsonDoesNotDuplicateUnlockRules() {
        String sourceJson = "{\"mission\":\"sample\",\"version\":2}";

        MissionVersion existingVersion = new MissionVersion();
        existingVersion.setId(versionId);
        existingVersion.setContentJson(sourceJson);

        when(missionRepository.findByMissionKey("sample"))
                .thenReturn(Optional.of(mission));

        when(missionVersionRepository.findByMissionIdAndVersionNumber(missionId, 2))
                .thenReturn(Optional.of(existingVersion));

        MissionVersion returnedVersion = importService.importMission(
                validDefinition(),
                sourceJson
        );

        assertSame(existingVersion, returnedVersion);

        verify(nodeUnlockRuleRepository, never())
                .saveAndFlush(any(NodeUnlockRule.class));

        verify(nodeUnlockConditionRepository, never())
                .saveAll(any());

        verify(missionNodeRepository, never())
                .save(any(MissionNode.class));
    }

    private MissionDefinitionDocument validDefinition() {
        return new MissionDefinitionDocument(
                1,
                new MissionDefinitionDocument.MissionData(
                        "sample",
                        "Sample",
                        "A test mission.",
                        2
                ),
                List.of(
                        new MissionDefinitionDocument.NodeData(
                                "entry",
                                "Entry",
                                "Start",
                                new MissionDefinitionDocument.Position(50, 10),
                                true
                        ),
                        new MissionDefinitionDocument.NodeData(
                                "vault",
                                "Vault",
                                "Locked room",
                                new MissionDefinitionDocument.Position(50, 90),
                                false
                        )
                ),
                List.of(),
                List.of(
                        new MissionDefinitionDocument.PuzzleData(
                                "archive-code",
                                "entry",
                                "Enter the code",
                                "B-1894",
                                false
                        )
                ),
                List.of(
                        new MissionDefinitionDocument.EvidenceData(
                                "catalogue-card",
                                "entry",
                                "Catalogue Card",
                                "Card"
                        ),
                        new MissionDefinitionDocument.EvidenceData(
                                "signed-note",
                                "entry",
                                "Signed Note",
                                "Note"
                        )
                ),
                List.of(),
                List.of(
                        new MissionDefinitionDocument.UnlockRuleData(
                                "vault",
                                "AND",
                                List.of(
                                        new MissionDefinitionDocument.UnlockConditionData(
                                                "EVIDENCE_DISCOVERED",
                                                "catalogue-card"
                                        ),
                                        new MissionDefinitionDocument.UnlockConditionData(
                                                "EVIDENCE_DISCOVERED",
                                                "signed-note"
                                        )
                                )
                        ),
                        new MissionDefinitionDocument.UnlockRuleData(
                                "vault",
                                "OR",
                                List.of(
                                        new MissionDefinitionDocument.UnlockConditionData(
                                                "PUZZLE_SOLVED",
                                                "archive-code"
                                        )
                                )
                        )
                )
        );
    }
}