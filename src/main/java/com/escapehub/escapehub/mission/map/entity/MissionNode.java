package com.escapehub.escapehub.mission.map.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "mission_nodes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"missionVersionId", "nodeKey"})
})
public class MissionNode {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID missionVersionId;

    @Column(nullable = false)
    private String nodeKey;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int mapX;

    @Column(nullable = false)
    private int mapY;

    @Column(nullable = false)
    private boolean isStartingNode;

    public MissionNode() {
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMissionVersionId() { return missionVersionId; }
    public void setMissionVersionId(UUID missionVersionId) { this.missionVersionId = missionVersionId; }

    public String getNodeKey() { return nodeKey; }
    public void setNodeKey(String nodeKey) { this.nodeKey = nodeKey; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMapX() { return mapX; }
    public void setMapX(int mapX) { this.mapX = mapX; }

    public int getMapY() { return mapY; }
    public void setMapY(int mapY) { this.mapY = mapY; }

    public boolean isStartingNode() { return isStartingNode; }
    public void setStartingNode(boolean startingNode) { isStartingNode = startingNode; }
}