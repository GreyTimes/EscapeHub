package com.escapehub.escapehub.evidence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "evidence", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nodeId", "evidenceKey"})
})
public class Evidence {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID nodeId;

    @Column(nullable = false)
    private String evidenceKey;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Evidence() {
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getNodeId() { return nodeId; }
    public void setNodeId(UUID nodeId) { this.nodeId = nodeId; }

    public String getEvidenceKey() { return evidenceKey; }
    public void setEvidenceKey(String evidenceKey) { this.evidenceKey = evidenceKey; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}