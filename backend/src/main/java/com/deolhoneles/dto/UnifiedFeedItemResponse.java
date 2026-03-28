package com.deolhoneles.dto;

import com.deolhoneles.entity.LegislativeActivity;
import com.deolhoneles.entity.Proposal;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UnifiedFeedItemResponse(
        String type,
        LocalDate date,
        FeedActivityItem activity,
        FeedProposalItem proposal
) {

    public static UnifiedFeedItemResponse fromActivity(
            LegislativeActivity a, List<DeputyVoteSummary> votes) {
        return new UnifiedFeedItemResponse(
                "VOTING",
                a.getVoteDate(),
                FeedActivityItem.from(a, votes),
                null
        );
    }

    public static UnifiedFeedItemResponse fromProposal(
            Proposal p, List<ProposalAuthorSummary> authors) {
        return new UnifiedFeedItemResponse(
                "PROPOSAL",
                p.getPresentationDate(),
                null,
                FeedProposalItem.from(p, authors)
        );
    }
}
