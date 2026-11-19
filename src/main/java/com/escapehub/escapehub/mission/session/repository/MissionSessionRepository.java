package com.escapehub.escapehub.mission.session.repository;

import com.escapehub.escapehub.mission.session.entity.MissionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MissionSessionRepository extends JpaRepository<MissionSession, UUID> {
}
