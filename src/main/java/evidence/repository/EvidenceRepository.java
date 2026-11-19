package com.escapehub.escapehub.evidence.repository;

import com.escapehub.escapehub.evidence.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {
}