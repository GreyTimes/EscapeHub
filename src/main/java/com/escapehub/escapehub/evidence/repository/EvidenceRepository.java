package com.escapehub.escapehub.evidence.repository;

import com.escapehub.escapehub.evidence.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {
    List<Evidence> findByNodeId(UUID nodeId);
    boolean existsByNodeIdAndEvidenceKey(UUID nodeId, String evidenceKey);
}
