package com.deolhoneles.dto;

import com.deolhoneles.entity.Event;
import java.util.List;

public record FeedEventItem(
        Long id,
        Long externalId,
        String eventType,
        String description,
        String agendaSummary,
        String situation,
        String startTime,
        String endTime,
        String location,
        String organCode,
        String organName,
        String videoUrl,
        List<EventDeputySummary> deputies
) {

    public static FeedEventItem from(Event e, List<EventDeputySummary> deputies) {
        return new FeedEventItem(
                e.getId(), e.getExternalId(), e.getEventType(),
                e.getDescription(), e.getAgendaSummary(), e.getSituation(),
                e.getStartTime() != null ? e.getStartTime().toString() : null,
                e.getEndTime() != null ? e.getEndTime().toString() : null,
                e.getLocation(), e.getOrganCode(), e.getOrganName(),
                e.getVideoUrl(), deputies
        );
    }
}
