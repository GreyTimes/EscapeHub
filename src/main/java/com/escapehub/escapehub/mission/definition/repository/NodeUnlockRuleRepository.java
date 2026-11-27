package com.escapehub.escapehub.mission.definition.repository;

import com.escapehub.escapehub.mission.definition.entity.NodeUnlockRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NodeUnlockRuleRepository extends JpaRepository<NodeUnlockRule, UUID> {
    List<NodeUnlockRule> findByMissionVersionId(UUID missionVersionId);

    List<NodeUnlockRule> findByMissionVersionIdAndTargetNodeId(UUID missionVersionId, UUID targetNodeId);
}