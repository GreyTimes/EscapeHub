package com.escapehub.escapehub.mission.definition.importer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MissionDefinitionValidatorTest {

    private final MissionDefinitionValidator validator = new MissionDefinitionValidator();

    @Test
    void acceptsValidMissionDefinition() {
        assertDoesNotThrow(() -> validator.validate(validDocument(List.of())));
    }

    @Test
    void acceptsExistingMissionDefinitionWithoutUnlockRules() {
        assertDoesNotThrow(() -> validator.validate(validDocument(null)));
    }

    @Test
    void acceptsAndOrRulesAndAllSupportedConditionTypes() {
        MissionDefinitionDocument document = validDocument(List.of(
                new MissionDefinitionDocument.UnlockRuleData(
                        "vault",
                        "AND",
                        List.of(
                                new MissionDefinitionDocument.UnlockConditionData("NODE_COMPLETED", "entry"),
                                new MissionDefinitionDocument.UnlockConditionData("PUZZLE_SOLVED", "archive-code"),
                                new MissionDefinitionDocument.UnlockConditionData("EVIDENCE_DISCOVERED", "catalogue-card")
                        )
                ),
                new MissionDefinitionDocument.UnlockRuleData(
                        "vault",
                        "OR",
                        List.of(new MissionDefinitionDocument.UnlockConditionData("PUZZLE_SOLVED", "archive-code"))
                )
        ));

        assertDoesNotThrow(() -> validator.validate(document));
    }

    @Test
    void rejectsUnknownConnectionTarget() {
        MissionDefinitionDocument document = new MissionDefinitionDocument(
                1,
                new MissionDefinitionDocument.MissionData("sample", "Sample", "", 1),
                List.of(node("entry", true)),
                List.of(new MissionDefinitionDocument.ConnectionData("entry", "missing")),
                List.of(), List.of(), List.of(), List.of()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(document)
        );

        assertTrue(exception.getMessage().contains("unknown node"));
    }

    @Test
    void rejectsDuplicateNodeKeys() {
        MissionDefinitionDocument document = new MissionDefinitionDocument(
                1,
                new MissionDefinitionDocument.MissionData("sample", "Sample", "", 1),
                List.of(node("entry", true), node("entry", false)),
                List.of(), List.of(), List.of(), List.of(), List.of()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(document)
        );
    }

    @Test
    void rejectsUnknownUnlockTargetNode() {
        assertInvalidRule(
                new MissionDefinitionDocument.UnlockRuleData(
                        "missing",
                        "AND",
                        List.of(
                                new MissionDefinitionDocument.UnlockConditionData(
                                        "NODE_COMPLETED",
                                        "entry"
                                )
                        )
                ),
                "targetNode references an unknown node"
        );
    }

    @Test
    void rejectsUnknownNodeCompletedReference() {
        assertInvalidRule(
                new MissionDefinitionDocument.UnlockRuleData(
                        "vault",
                        "AND",
                        List.of(
                                new MissionDefinitionDocument.UnlockConditionData(
                                        "NODE_COMPLETED",
                                        "missing"
                                )
                        )
                ),
                "references an unknown node"
        );
    }

    @Test
    void rejectsUnknownPuzzleReference() {
        assertInvalidRule(
                new MissionDefinitionDocument.UnlockRuleData(
                        "vault",
                        "AND",
                        List.of(
                                new MissionDefinitionDocument.UnlockConditionData(
                                        "PUZZLE_SOLVED",
                                        "missing-puzzle"
                                )
                        )
                ),
                "references an unknown puzzle"
        );
    }

    @Test
    void rejectsUnknownEvidenceReference() {
        assertInvalidRule(
                new MissionDefinitionDocument.UnlockRuleData(
                        "vault",
                        "AND",
                        List.of(
                                new MissionDefinitionDocument.UnlockConditionData(
                                        "EVIDENCE_DISCOVERED",
                                        "missing-evidence"
                                )
                        )
                ),
                "references an unknown evidence"
        );
    }

    @Test
    void rejectsUnknownOperator() {
        assertInvalidRule(
                new MissionDefinitionDocument.UnlockRuleData(
                        "vault",
                        "XOR",
                        List.of(
                                new MissionDefinitionDocument.UnlockConditionData(
                                        "NODE_COMPLETED",
                                        "entry"
                                )
                        )
                ),
                "operator must be AND or OR"
        );
    }

    @Test
    void rejectsUnknownConditionType() {
        assertInvalidRule(
                new MissionDefinitionDocument.UnlockRuleData(
                        "vault",
                        "AND",
                        List.of(
                                new MissionDefinitionDocument.UnlockConditionData(
                                        "ITEM_USED",
                                        "brass-key"
                                )
                        )
                ),
                "must be one of NODE_COMPLETED"
        );
    }

    @Test
    void rejectsUnlockRuleWithoutConditions() {
        assertInvalidRule(
                new MissionDefinitionDocument.UnlockRuleData(
                        "vault",
                        "AND",
                        List.of()
                ),
                "conditions must contain at least one condition"
        );
    }

    private void assertInvalidRule(
            MissionDefinitionDocument.UnlockRuleData rule,
            String expectedMessage
    ) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(validDocument(List.of(rule)))
        );

        assertTrue(
                exception.getMessage().contains(expectedMessage),
                exception.getMessage()
        );
    }

    private MissionDefinitionDocument validDocument(
            List<MissionDefinitionDocument.UnlockRuleData> unlockRules
    ) {
        return new MissionDefinitionDocument(
                1,
                new MissionDefinitionDocument.MissionData(
                        "sample",
                        "Sample",
                        "A test mission.",
                        1
                ),
                List.of(
                        node("entry", true),
                        node("vault", false)
                ),
                List.of(
                        new MissionDefinitionDocument.ConnectionData(
                                "entry",
                                "vault"
                        )
                ),
                List.of(
                        new MissionDefinitionDocument.PuzzleData(
                                "archive-code",
                                "entry",
                                "What is the code?",
                                "B-1894",
                                false
                        )
                ),
                List.of(
                        new MissionDefinitionDocument.EvidenceData(
                                "catalogue-card",
                                "entry",
                                "Catalogue Card",
                                "A reference card."
                        )
                ),
                List.of(),
                unlockRules
        );
    }

    private MissionDefinitionDocument.NodeData node(
            String key,
            boolean startNode
    ) {
        return new MissionDefinitionDocument.NodeData(
                key,
                key,
                "Description for " + key,
                new MissionDefinitionDocument.Position(50, 50),
                startNode
        );
    }
}