package com.escapehub.escapehub.mission.map.repository;

import com.escapehub.escapehub.mission.map.entity.NodeConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NodeConnectionRepository extends JpaRepository<NodeConnection, UUID> {
    boolean existsBySourceNodeIdAndTargetNodeId(UUID sourceNodeId, UUID targetNodeId);
}