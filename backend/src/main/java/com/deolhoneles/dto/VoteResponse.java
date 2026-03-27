package com.deolhoneles.dto;

import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.entity.VoteType;

public record VoteResponse(
        Long id,
        Long deputyId,
        Long proposalId,
        VoteType vote
) {

    public static VoteResponse from(DeputyVote entity) {
        return new VoteResponse(
                entity.getId(),
                entity.getDeputyId(),
                entity.getProposalId(),
                entity.getVote()
        );
    }
}
