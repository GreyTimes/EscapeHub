package com.escapehub.escapehub.evidence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionEvidenceDiscoveryRepository extends JpaRepository<SessionEvidenceDiscovery, UUID> {

    boolean existsBySessionIdAndEvidenceId(UUID sessionId, UUID evidenceId);

    List<SessionEvidenceDiscovery> findAllBySessionIdOrderByDiscoveredAtAsc(UUID sessionId);
}