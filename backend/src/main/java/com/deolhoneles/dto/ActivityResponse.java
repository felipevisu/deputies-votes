package com.deolhoneles.dto;

import com.deolhoneles.entity.LegislativeActivity;
import java.time.LocalDate;

public record ActivityResponse(
        Long id,
        String title,
        String subtitle,
        String summary,
        String author,
        String category,
        LocalDate voteDate,
        String externalId,
        String description,
        String sourceProposalId
) {

    public static ActivityResponse from(LegislativeActivity entity) {
        return new ActivityResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getSubtitle(),
                entity.getSummary(),
                entity.getAuthor(),
                entity.getCategory(),
                entity.getVoteDate(),
                entity.getExternalId(),
                entity.getDescription(),
                entity.getSourceProposalId()
        );
    }
}
