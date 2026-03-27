package com.deolhoneles.dto;

import com.deolhoneles.entity.Deputy;
import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.entity.LegislativeProposal;
import com.deolhoneles.entity.VoteType;
import java.time.LocalDate;

public record FeedItemResponse(
        Long id,
        String name,
        String description,
        String deputieName,
        String deputieParty,
        VoteType vote,
        String category,
        String author,
        LocalDate voteDate,
        Long deputyId
) {

    public static FeedItemResponse from(DeputyVote dv) {
        Deputy d = dv.getDeputy();
        LegislativeProposal p = dv.getProposal();
        return new FeedItemResponse(
                dv.getId(),
                p.getTitle(),
                p.getSummary(),
                d.getName(),
                d.getParty() + " - " + d.getState(),
                dv.getVote(),
                p.getCategory(),
                p.getAuthor(),
                p.getVoteDate(),
                d.getId()
        );
    }
}
