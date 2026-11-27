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

/**
 * One unlock path for a target node. Separate rules for the same target are
 * alternative paths (OR between rules); the operator applies within this rule.
 */
@Entity
@Table(
        name = "node_unlock_rules",
        indexes = @Index(
                name = "idx_node_unlock_rules_version_target",
                columnList = "mission_version_id, target_node_id"
        )
)
public class NodeUnlockRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "mission_version_id", nullable = false)
    private UUID missionVersionId;

    @Column(name = "target_node_id", nullable = false)
    private UUID targetNodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "unlock_operator", nullable = false, length = 3)
    private UnlockOperator operator;

    public NodeUnlockRule() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMissionVersionId() { return missionVersionId; }
    public void setMissionVersionId(UUID missionVersionId) { this.missionVersionId = missionVersionId; }

    public UUID getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(UUID targetNodeId) { this.targetNodeId = targetNodeId; }

    public UnlockOperator getOperator() { return operator; }
    public void setOperator(UnlockOperator operator) { this.operator = operator; }
}