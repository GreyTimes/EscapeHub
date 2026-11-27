package com.escapehub.escapehub.mission.definition.repository;

import com.escapehub.escapehub.mission.definition.entity.NodeUnlockCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NodeUnlockConditionRepository extends JpaRepository<NodeUnlockCondition, UUID> {
    List<NodeUnlockCondition> findByNodeUnlockRuleId(UUID nodeUnlockRuleId);

    List<NodeUnlockCondition> findByNodeUnlockRuleIdIn(Collection<UUID> nodeUnlockRuleIds);
}