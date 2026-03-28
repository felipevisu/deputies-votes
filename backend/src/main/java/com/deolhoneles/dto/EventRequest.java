package com.deolhoneles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(
        @NotNull Long externalId,
        @NotBlank String eventType,
        String description,
        String agendaSummary,
        String situation,
        @NotBlank String startTime,
        String endTime,
        String location,
        String organCode,
        String organName,
        String videoUrl,
        @NotBlank String eventDate
) {
}
