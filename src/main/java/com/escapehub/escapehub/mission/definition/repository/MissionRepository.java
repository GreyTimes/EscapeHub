package com.escapehub.escapehub.mission.definition.repository;

import com.escapehub.escapehub.mission.definition.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MissionRepository extends JpaRepository<Mission, UUID> {
}