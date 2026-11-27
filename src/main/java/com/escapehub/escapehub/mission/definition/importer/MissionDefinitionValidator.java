package com.escapehub.escapehub.mission.definition.importer;

import com.escapehub.escapehub.mission.definition.entity.UnlockConditionType;
import com.escapehub.escapehub.mission.definition.entity.UnlockOperator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.escapehub.escapehub.mission.definition.importer.MissionDefinitionDocument.*;

@Component
public class MissionDefinitionValidator {

    public void validate(MissionDefinitionDocument document) {
        List<String> errors = new ArrayList<>();

        if (document == null) {
            throw new IllegalArgumentException("Mission JSON must contain a JSON object.");
        }

        if (document.schemaVersion() == null || document.schemaVersion() != 1) {
            errors.add("schemaVersion must be 1.");
        }

        MissionData mission = document.mission();

        if (mission == null) {
            errors.add("mission is required.");
        } else {
            requireText(mission.key(), "mission.key", errors);
            requireText(mission.title(), "mission.title", errors);

            if (mission.versionNumber() == null || mission.versionNumber() < 1) {
                errors.add("mission.versionNumber must be greater than zero.");
            }
        }

        List<NodeData> nodes = safe(document.nodes());

        if (nodes.isEmpty()) {
            errors.add("At least one node is required.");
        }

        Set<String> nodeKeys = new HashSet<>();
        int startNodeCount = 0;

        for (int i = 0; i < nodes.size(); i++) {
            NodeData node = nodes.get(i);
            String path = "nodes[" + i + "]";

            if (node == null) {
                errors.add(path + " must not be null.");
                continue;
            }

            requireText(node.key(), path + ".key", errors);
            requireText(node.title(), path + ".title", errors);

            if (node.key() != null && !node.key().isBlank() && !nodeKeys.add(node.key())) {
                errors.add("Duplicate node key: " + node.key() + ".");
            }

            if (Boolean.TRUE.equals(node.startNode())) {
                startNodeCount++;
            }

            if (node.position() == null
                    || node.position().x() == null
                    || node.position().y() == null) {

                errors.add(path + ".position must contain numeric x and y coordinates.");

            } else if (node.position().x() < 0 || node.position().x() > 100
                    || node.position().y() < 0 || node.position().y() > 100) {

                errors.add(path + ".position coordinates must be between 0 and 100.");
            }
        }

        if (startNodeCount != 1) {
            errors.add("Exactly one node must have startNode=true.");
        }

        Set<String> connectionKeys = new HashSet<>();
        List<ConnectionData> connections = safe(document.connections());

        for (int i = 0; i < connections.size(); i++) {
            ConnectionData connection = connections.get(i);

            if (connection == null) {
                errors.add("connections[" + i + "] must not be null.");
                continue;
            }

            requireNodeReference(
                    connection.from(),
                    "connections[" + i + "].from",
                    nodeKeys,
                    errors
            );

            requireNodeReference(
                    connection.to(),
                    "connections[" + i + "].to",
                    nodeKeys,
                    errors
            );

            if (connection.from() != null && connection.from().equals(connection.to())) {
                errors.add("A node cannot connect to itself: " + connection.from() + ".");
            }

            String connectionKey = connection.from() + "->" + connection.to();

            if (!connectionKeys.add(connectionKey)) {
                errors.add("Duplicate connection: " + connectionKey + ".");
            }
        }

        Set<String> puzzleKeys = validatePuzzleKeys(
                safe(document.puzzles()),
                nodeKeys,
                errors
        );

        Set<String> evidenceKeys = validateEvidenceKeys(
                safe(document.evidence()),
                nodeKeys,
                errors
        );

        validateItemKeys(
                safe(document.inventoryItems()),
                nodeKeys,
                errors
        );

        validateUnlockRules(
                safe(document.unlockRules()),
                nodeKeys,
                puzzleKeys,
                evidenceKeys,
                errors
        );

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid mission definition:\n - " + String.join("\n - ", errors)
            );
        }
    }

    private Set<String> validatePuzzleKeys(
            List<PuzzleData> puzzles,
            Set<String> nodeKeys,
            List<String> errors
    ) {
        Set<String> keys = new HashSet<>();

        for (int i = 0; i < puzzles.size(); i++) {
            PuzzleData puzzle = puzzles.get(i);
            String path = "puzzles[" + i + "]";

            if (puzzle == null) {
                errors.add(path + " must not be null.");
                continue;
            }

            requireText(puzzle.key(), path + ".key", errors);
            requireNodeReference(puzzle.node(), path + ".node", nodeKeys, errors);
            requireText(puzzle.question(), path + ".question", errors);
            requireText(puzzle.correctAnswer(), path + ".correctAnswer", errors);

            addUniqueKey(puzzle.key(), "puzzle", keys, errors);
        }

        return keys;
    }

    private Set<String> validateEvidenceKeys(
            List<EvidenceData> evidence,
            Set<String> nodeKeys,
            List<String> errors
    ) {
        Set<String> keys = new HashSet<>();

        for (int i = 0; i < evidence.size(); i++) {
            EvidenceData item = evidence.get(i);
            String path = "evidence[" + i + "]";

            if (item == null) {
                errors.add(path + " must not be null.");
                continue;
            }

            requireText(item.key(), path + ".key", errors);
            requireNodeReference(item.node(), path + ".node", nodeKeys, errors);
            requireText(item.title(), path + ".title", errors);

            addUniqueKey(item.key(), "evidence", keys, errors);
        }

        return keys;
    }

    private void validateItemKeys(
            List<InventoryItemData> items,
            Set<String> nodeKeys,
            List<String> errors
    ) {
        Set<String> keys = new HashSet<>();

        for (int i = 0; i < items.size(); i++) {
            InventoryItemData item = items.get(i);
            String path = "inventoryItems[" + i + "]";

            if (item == null) {
                errors.add(path + " must not be null.");
                continue;
            }

            requireText(item.key(), path + ".key", errors);
            requireNodeReference(item.node(), path + ".node", nodeKeys, errors);
            requireText(item.title(), path + ".title", errors);

            addUniqueKey(item.key(), "inventory item", keys, errors);
        }
    }

    private void validateUnlockRules(
            List<UnlockRuleData> rules,
            Set<String> nodeKeys,
            Set<String> puzzleKeys,
            Set<String> evidenceKeys,
            List<String> errors
    ) {
        for (int i = 0; i < rules.size(); i++) {
            UnlockRuleData rule = rules.get(i);
            String rulePath = "unlockRules[" + i + "]";

            if (rule == null) {
                errors.add(rulePath + " must not be null.");
                continue;
            }

            requireNodeReference(
                    rule.targetNode(),
                    rulePath + ".targetNode",
                    nodeKeys,
                    errors
            );

            validateOperator(rule.operator(), rulePath + ".operator", errors);

            List<UnlockConditionData> conditions = safe(rule.conditions());

            if (conditions.isEmpty()) {
                errors.add(rulePath + ".conditions must contain at least one condition.");
            }

            for (int conditionIndex = 0; conditionIndex < conditions.size(); conditionIndex++) {
                UnlockConditionData condition = conditions.get(conditionIndex);

                String conditionPath = rulePath + ".conditions[" + conditionIndex + "]";

                if (condition == null) {
                    errors.add(conditionPath + " must not be null.");
                    continue;
                }

                UnlockConditionType type = validateConditionType(
                        condition.type(),
                        conditionPath + ".type",
                        errors
                );

                requireText(
                        condition.reference(),
                        conditionPath + ".reference",
                        errors
                );

                if (condition.reference() == null || type == null) {
                    continue;
                }

                switch (type) {
                    case NODE_COMPLETED -> requireTypedReference(
                            condition.reference(),
                            "node",
                            nodeKeys,
                            conditionPath + ".reference",
                            errors
                    );

                    case PUZZLE_SOLVED -> requireTypedReference(
                            condition.reference(),
                            "puzzle",
                            puzzleKeys,
                            conditionPath + ".reference",
                            errors
                    );

                    case EVIDENCE_DISCOVERED -> requireTypedReference(
                            condition.reference(),
                            "evidence",
                            evidenceKeys,
                            conditionPath + ".reference",
                            errors
                    );
                }
            }
        }
    }

    private void validateOperator(
            String value,
            String path,
            List<String> errors
    ) {
        if (value == null || value.isBlank()) {
            errors.add(path + " is required (AND or OR).");
            return;
        }

        try {
            UnlockOperator.valueOf(value);
        } catch (IllegalArgumentException exception) {
            errors.add(path + " must be AND or OR; found: " + value + ".");
        }
    }

    private UnlockConditionType validateConditionType(
            String value,
            String path,
            List<String> errors
    ) {
        if (value == null || value.isBlank()) {
            errors.add(path + " is required.");
            return null;
        }

        try {
            return UnlockConditionType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            errors.add(
                    path + " must be one of NODE_COMPLETED, PUZZLE_SOLVED, "
                            + "EVIDENCE_DISCOVERED; found: " + value + "."
            );
            return null;
        }
    }

    private void requireTypedReference(
            String value,
            String type,
            Set<String> validKeys,
            String path,
            List<String> errors
    ) {
        if (!validKeys.contains(value)) {
            errors.add(path + " references an unknown " + type + ": " + value + ".");
        }
    }

    private void addUniqueKey(
            String key,
            String type,
            Set<String> keys,
            List<String> errors
    ) {
        if (key != null && !key.isBlank() && !keys.add(key)) {
            errors.add("Duplicate " + type + " key: " + key + ".");
        }
    }

    private void requireNodeReference(
            String value,
            String path,
            Set<String> nodeKeys,
            List<String> errors
    ) {
        requireText(value, path, errors);

        if (value != null && !nodeKeys.contains(value)) {
            errors.add(path + " references an unknown node: " + value + ".");
        }
    }

    private void requireText(
            String value,
            String path,
            List<String> errors
    ) {
        if (value == null || value.isBlank()) {
            errors.add(path + " is required.");
        }
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}