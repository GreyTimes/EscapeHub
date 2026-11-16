package com.escapehub.escapehub.mission.map.repository;

import com.escapehub.escapehub.mission.map.entity.MissionNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MissionNodeRepository extends JpaRepository<MissionNode, UUID> {
    List<MissionNode> findByMissionVersionId(UUID missionVersionId);
    Optional<MissionNode> findByMissionVersionIdAndNodeKey(UUID missionVersionId, String nodeKey);
}