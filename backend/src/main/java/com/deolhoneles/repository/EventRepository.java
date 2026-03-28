package com.deolhoneles.repository;

import com.deolhoneles.entity.Event;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByExternalId(Long externalId);

    @Query(value = """
            SELECT DISTINCT e FROM Event e
            JOIN EventDeputy ed ON ed.eventId = e.id
            WHERE ed.deputyId IN :deputyIds
            ORDER BY e.eventDate DESC, e.id ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT e.id) FROM Event e
            JOIN EventDeputy ed ON ed.eventId = e.id
            WHERE ed.deputyId IN :deputyIds
            """)
    Page<Event> findEventsByDeputyIds(
            @Param("deputyIds") List<Long> deputyIds, Pageable pageable);
}
