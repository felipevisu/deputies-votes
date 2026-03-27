package com.deolhoneles.dto;

import java.time.LocalDate;
import java.util.List;

public record ProposalFeedItemResponse(
        Long proposalId,
        String name,
        String description,
        String author,
        LocalDate voteDate,
        List<DeputyVoteSummary> votes
) {
}
