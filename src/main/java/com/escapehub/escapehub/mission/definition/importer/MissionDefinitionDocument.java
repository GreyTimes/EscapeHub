package com.escapehub.escapehub.mission.definition.importer;

import java.util.List;

/** JSON document describing the currently supported mission-definition subset. */
public record MissionDefinitionDocument(
        Integer schemaVersion,
        MissionData mission,
        List<NodeData> nodes,
        List<ConnectionData> connections,
        List<PuzzleData> puzzles,
        List<EvidenceData> evidence,
        List<InventoryItemData> inventoryItems,
        List<UnlockRuleData> unlockRules
) {
    public record MissionData(String key, String title, String description, Integer versionNumber) {
    }

    public record Position(Integer x, Integer y) {
    }

    public record NodeData(
            String key,
            String title,
            String description,
            Position position,
            Boolean startNode
    ) {
    }

    public record ConnectionData(String from, String to) {
    }

    public record PuzzleData(
            String key,
            String node,
            String question,
            String correctAnswer,
            Boolean finalPuzzle
    ) {
    }

    public record EvidenceData(String key, String node, String title, String description) {
    }

    public record InventoryItemData(String key, String node, String title, String description) {
    }

    public record UnlockRuleData(
            String targetNode,
            String operator,
            List<UnlockConditionData> conditions
    ) {
    }

    public record UnlockConditionData(String type, String reference) {
    }
}