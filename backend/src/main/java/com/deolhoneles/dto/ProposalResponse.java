package com.deolhoneles.dto;

import com.deolhoneles.entity.LegislativeProposal;
import java.time.LocalDate;

public record ProposalResponse(
        Long id,
        String title,
        String summary,
        String author,
        String category,
        LocalDate voteDate,
        String externalId
) {

    public static ProposalResponse from(LegislativeProposal entity) {
        return new ProposalResponse(
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
