package com.deolhoneles.repository;

import com.deolhoneles.entity.DeputyVote;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeputyVoteRepository extends JpaRepository<DeputyVote, Long> {

    @Query(value = """
            SELECT dv FROM DeputyVote dv
            JOIN FETCH dv.deputy d
            JOIN FETCH dv.activity a
            WHERE d.id IN :deputyIds
            ORDER BY a.voteDate DESC, dv.id ASC
            """,
            countQuery = """
            SELECT COUNT(dv) FROM DeputyVote dv
            WHERE dv.deputyId IN :deputyIds
            """)
    Page<DeputyVote> findFeedItemsByDeputyIds(@Param("deputyIds") List<Long> deputyIds, Pageable pageable);

    @Query("""
            SELECT dv FROM DeputyVote dv
            JOIN FETCH dv.deputy d
            JOIN FETCH dv.activity a
            WHERE dv.activityId IN :activityIds
            AND dv.deputyId IN :deputyIds
            ORDER BY d.name ASC
            """)
    List<DeputyVote> findVotesByActivityIdsAndDeputyIds(
            @Param("activityIds") List<Long> activityIds,
            @Param("deputyIds") List<Long> deputyIds);
}
