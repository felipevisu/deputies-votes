package com.deolhoneles.repository;

import com.deolhoneles.entity.EventDeputy;
import com.deolhoneles.entity.EventDeputyId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventDeputyRepository extends JpaRepository<EventDeputy, EventDeputyId> {

    @Query("""
            SELECT ed FROM EventDeputy ed
            JOIN FETCH ed.deputy d
            WHERE ed.eventId IN :eventIds
            AND ed.deputyId IN :deputyIds
            ORDER BY d.name ASC
            """)
    List<EventDeputy> findByEventIdsAndDeputyIds(
            @Param("eventIds") List<Long> eventIds,
            @Param("deputyIds") List<Long> deputyIds);

    boolean existsByEventIdAndDeputyId(Long eventId, Long deputyId);
}
