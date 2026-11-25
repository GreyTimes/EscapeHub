
package com.escapehub.escapehub.mission.session.repository;

import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.entity.ParticipationStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionMemberRepository extends JpaRepository<SessionMember, UUID> {

    List<SessionMember> findBySessionId(UUID sessionId);

    Optional<SessionMember> findBySessionIdAndUserId(UUID sessionId, UUID userId);

    boolean existsBySessionIdAndUserId(UUID sessionId, UUID userId);

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

    @Query("""
            select s
            from SessionMember m, MissionSession s
            where s.id = m.sessionId
              and m.userId = :userId
              and m.participationStatus = :participationStatus
              and s.status = :sessionStatus
            order by s.startedAt desc
            """)
    List<MissionSession> findActiveSessionsForUser(
            @Param("userId") UUID userId,
            @Param("participationStatus") ParticipationStatus participationStatus,
            @Param("sessionStatus") MissionSessionStatus sessionStatus
    );
}
