package com.deolhoneles.dto;

import com.deolhoneles.entity.LegislativeActivity;
import java.time.LocalDate;

public record ActivityResponse(
        Long id,
        String title,
        String summary,
        String author,
        String category,
        LocalDate voteDate,
        String externalId
) {

    public static ActivityResponse from(LegislativeActivity entity) {
        return new ActivityResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getAuthor(),
                entity.getCategory(),
                entity.getVoteDate(),
                entity.getExternalId()
        );
    }
}
