package com.escapehub.escapehub.mission.map.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "node_connections", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sourceNodeId", "targetNodeId"})
})
public class NodeConnection {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID sourceNodeId;

    @Column(nullable = false)
    private UUID targetNodeId;

    public NodeConnection() {
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(UUID sourceNodeId) { this.sourceNodeId = sourceNodeId; }

    public UUID getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(UUID targetNodeId) { this.targetNodeId = targetNodeId; }
}