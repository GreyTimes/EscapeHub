package com.escapehub.escapehub.evidence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
        name = "evidence",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_evidence_node_key",
                columnNames = {"node_id", "evidence_key"}
        )
)
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "evidence_key", nullable = false, length = 100)
    private String evidenceKey;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Evidence() {
    }

    public Evidence(UUID nodeId, String evidenceKey, String title, String description) {
        this.nodeId = nodeId;
        this.evidenceKey = evidenceKey;
        this.title = title;
        this.description = description;
    }

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
