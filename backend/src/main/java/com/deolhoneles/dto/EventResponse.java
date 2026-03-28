package com.deolhoneles.dto;

import com.deolhoneles.entity.Event;

public record EventResponse(
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
        String eventDate
) {

    public static EventResponse from(Event entity) {
        return new EventResponse(
                entity.getId(),
                entity.getExternalId(),
                entity.getEventType(),
                entity.getDescription(),
                entity.getAgendaSummary(),
                entity.getSituation(),
                entity.getStartTime() != null ? entity.getStartTime().toString() : null,
                entity.getEndTime() != null ? entity.getEndTime().toString() : null,
                entity.getLocation(),
                entity.getOrganCode(),
                entity.getOrganName(),
                entity.getVideoUrl(),
                entity.getEventDate() != null ? entity.getEventDate().toString() : null
        );
    }
}
