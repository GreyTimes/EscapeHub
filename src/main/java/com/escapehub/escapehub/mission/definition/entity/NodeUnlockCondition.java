package com.escapehub.escapehub.mission.definition.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(
        name = "node_unlock_conditions",
        indexes = @Index(
                name = "idx_node_unlock_conditions_rule",
                columnList = "node_unlock_rule_id"
        )
)
public class NodeUnlockCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "node_unlock_rule_id", nullable = false)
    private UUID nodeUnlockRuleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 32)
    private UnlockConditionType conditionType;

    @Column(name = "reference_key", nullable = false, length = 100)
    private String referenceKey;

    public NodeUnlockCondition() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getNodeUnlockRuleId() { return nodeUnlockRuleId; }
    public void setNodeUnlockRuleId(UUID nodeUnlockRuleId) { this.nodeUnlockRuleId = nodeUnlockRuleId; }

    public UnlockConditionType getConditionType() { return conditionType; }
    public void setConditionType(UnlockConditionType conditionType) { this.conditionType = conditionType; }

    public String getReferenceKey() { return referenceKey; }
    public void setReferenceKey(String referenceKey) { this.referenceKey = referenceKey; }
}