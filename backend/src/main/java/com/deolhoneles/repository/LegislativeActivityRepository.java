package com.deolhoneles.repository;

import com.deolhoneles.entity.LegislativeActivity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LegislativeActivityRepository extends JpaRepository<LegislativeActivity, Long> {

    Optional<LegislativeActivity> findByExternalId(String externalId);

    @Query(value = """
            SELECT DISTINCT a FROM LegislativeActivity a
            JOIN DeputyVote dv ON dv.activityId = a.id
            WHERE dv.deputyId IN :deputyIds
            ORDER BY a.voteDate DESC, a.id ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT a.id) FROM LegislativeActivity a
            JOIN DeputyVote dv ON dv.activityId = a.id
            WHERE dv.deputyId IN :deputyIds
            """)
    Page<LegislativeActivity> findActivitiesVotedByDeputyIds(
            @Param("deputyIds") List<Long> deputyIds, Pageable pageable);
}
