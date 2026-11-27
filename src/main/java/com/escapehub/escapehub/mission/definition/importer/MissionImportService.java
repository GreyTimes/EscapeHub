package com.escapehub.escapehub.mission.definition.importer;

import com.escapehub.escapehub.evidence.entity.Evidence;
import com.escapehub.escapehub.evidence.repository.EvidenceRepository;
import com.escapehub.escapehub.inventory.entity.InventoryItem;
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
import com.escapehub.escapehub.mission.map.entity.NodeConnection;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.map.repository.NodeConnectionRepository;
import com.escapehub.escapehub.puzzle.entity.Puzzle;
import com.escapehub.escapehub.puzzle.repository.PuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MissionImportService {

    private final MissionRepository missionRepository;
    private final MissionVersionRepository missionVersionRepository;
    private final MissionNodeRepository nodeRepository;
    private final NodeConnectionRepository connectionRepository;
    private final PuzzleRepository puzzleRepository;
    private final EvidenceRepository evidenceRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final NodeUnlockRuleRepository nodeUnlockRuleRepository;
    private final NodeUnlockConditionRepository nodeUnlockConditionRepository;
    private final MissionDefinitionValidator validator;

    public MissionImportService(
            MissionRepository missionRepository,
            MissionVersionRepository missionVersionRepository,
            MissionNodeRepository nodeRepository,
            NodeConnectionRepository connectionRepository,
            PuzzleRepository puzzleRepository,
            EvidenceRepository evidenceRepository,
            InventoryItemRepository inventoryItemRepository,
            NodeUnlockRuleRepository nodeUnlockRuleRepository,
            NodeUnlockConditionRepository nodeUnlockConditionRepository,
            MissionDefinitionValidator validator
    ) {
        this.missionRepository = missionRepository;
        this.missionVersionRepository = missionVersionRepository;
        this.nodeRepository = nodeRepository;
        this.connectionRepository = connectionRepository;
        this.puzzleRepository = puzzleRepository;
        this.evidenceRepository = evidenceRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.nodeUnlockRuleRepository = nodeUnlockRuleRepository;
        this.nodeUnlockConditionRepository = nodeUnlockConditionRepository;
        this.validator = validator;
    }

    @Transactional
    public MissionVersion importMission(
            MissionDefinitionDocument definition,
            String sourceJson
    ) {
        validator.validate(definition);

        if (sourceJson == null || sourceJson.isBlank()) {
            throw new IllegalArgumentException("Original mission JSON is required.");
        }

        Mission mission = missionRepository.findByMissionKey(definition.mission().key())
                .orElseGet(() -> {
                    Mission created = new Mission();
                    created.setMissionKey(definition.mission().key());
                    created.setTitle(definition.mission().title());
                    created.setDescription(definition.mission().description());
                    return missionRepository.saveAndFlush(created);
                });

        // Mission versions are immutable. Re-importing identical JSON is a no-op;
        // changing existing version content requires a new version number.
        var existingVersion = missionVersionRepository.findByMissionIdAndVersionNumber(
                mission.getId(),
                definition.mission().versionNumber()
        );

        if (existingVersion.isPresent()) {
            MissionVersion existing = existingVersion.get();

            if (!existing.getContentJson().equals(sourceJson)) {
                throw new IllegalStateException(
                        "Mission version " + definition.mission().versionNumber()
                                + " already exists with different content. Create a new version instead."
                );
            }

            return existing;
        }

        MissionVersion version = new MissionVersion();
        version.setMission(mission);
        version.setVersionNumber(definition.mission().versionNumber());
        version.setSchemaVersion(definition.schemaVersion());
        version.setContentJson(sourceJson);
        version = missionVersionRepository.saveAndFlush(version);

        Map<String, MissionNode> nodesByKey = new HashMap<>();

        for (MissionDefinitionDocument.NodeData nodeData : definition.nodes()) {
            MissionNode node = new MissionNode();

            node.setMissionVersionId(version.getId());
            node.setNodeKey(nodeData.key());
            node.setTitle(nodeData.title());
            node.setDescription(nodeData.description());
            node.setMapX(nodeData.position().x());
            node.setMapY(nodeData.position().y());
            node.setStartingNode(Boolean.TRUE.equals(nodeData.startNode()));

            node = nodeRepository.save(node);
            nodesByKey.put(nodeData.key(), node);
        }

        for (MissionDefinitionDocument.ConnectionData connectionData
                : safe(definition.connections())) {

            NodeConnection connection = new NodeConnection();

            connection.setSourceNodeId(
                    nodesByKey.get(connectionData.from()).getId()
            );

            connection.setTargetNodeId(
                    nodesByKey.get(connectionData.to()).getId()
            );

            connectionRepository.save(connection);
        }

        for (MissionDefinitionDocument.PuzzleData puzzleData
                : safe(definition.puzzles())) {

            MissionNode node = nodesByKey.get(puzzleData.node());

            puzzleRepository.save(new Puzzle(
                    node.getId(),
                    puzzleData.key(),
                    puzzleData.question(),
                    puzzleData.correctAnswer(),
                    Boolean.TRUE.equals(puzzleData.finalPuzzle())
            ));
        }

        for (MissionDefinitionDocument.EvidenceData evidenceData
                : safe(definition.evidence())) {

            Evidence evidence = new Evidence(
                    nodesByKey.get(evidenceData.node()).getId(),
                    evidenceData.key(),
                    evidenceData.title(),
                    evidenceData.description()
            );

            evidenceRepository.save(evidence);
        }

        for (MissionDefinitionDocument.InventoryItemData itemData
                : safe(definition.inventoryItems())) {

            InventoryItem item = new InventoryItem();

            item.setNodeId(nodesByKey.get(itemData.node()).getId());
            item.setItemKey(itemData.key());
            item.setTitle(itemData.title());
            item.setDescription(itemData.description());

            inventoryItemRepository.save(item);
        }

        // Persist unlock rules and their conditions for this MissionVersion.
        for (MissionDefinitionDocument.UnlockRuleData ruleData
                : safe(definition.unlockRules())) {

            NodeUnlockRule rule = new NodeUnlockRule();

            rule.setMissionVersionId(version.getId());
            rule.setTargetNodeId(
                    nodesByKey.get(ruleData.targetNode()).getId()
            );
            rule.setOperator(UnlockOperator.valueOf(ruleData.operator()));

            rule = nodeUnlockRuleRepository.saveAndFlush(rule);

            List<NodeUnlockCondition> conditions = new ArrayList<>();

            for (MissionDefinitionDocument.UnlockConditionData conditionData
                    : safe(ruleData.conditions())) {

                NodeUnlockCondition condition = new NodeUnlockCondition();

                condition.setNodeUnlockRuleId(rule.getId());
                condition.setConditionType(
                        UnlockConditionType.valueOf(conditionData.type())
                );
                condition.setReferenceKey(conditionData.reference());

                conditions.add(condition);
            }

            nodeUnlockConditionRepository.saveAll(conditions);
        }

        return version;
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}