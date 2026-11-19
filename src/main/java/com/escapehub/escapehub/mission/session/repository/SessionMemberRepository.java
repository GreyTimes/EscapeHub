package com.escapehub.escapehub.mission.session.repository;

import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionMemberRepository extends JpaRepository<SessionMember, UUID> {

    List<SessionMember> findBySessionId(UUID sessionId);

    boolean existsBySessionIdAndUserId(UUID sessionId, UUID userId);

    /**
     * True if the user is a member of a session with the given status.
     * A member who LEFT still counts, because they can return to the session.
     */
    @Query("""
            select case when count(m) > 0 then true else false end
            from SessionMember m, MissionSession s
            where s.id = m.sessionId
              and m.userId = :userId
              and s.status = :status
            """)
    boolean existsByUserIdAndSessionStatus(
            @Param("userId") UUID userId,
            @Param("status") MissionSessionStatus status
    );
}
