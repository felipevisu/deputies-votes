package com.deolhoneles.dto;

import com.deolhoneles.entity.Deputy;
import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.entity.VoteType;

public record DeputyVoteSummary(
        Long deputyId,
        String name,
        String party,
        String state,
        String photo,
        VoteType vote
) {

    public static DeputyVoteSummary from(DeputyVote dv) {
        Deputy d = dv.getDeputy();
        return new DeputyVoteSummary(
                d.getId(),
                d.getName(),
                d.getParty(),
                d.getState(),
                d.getAvatar(),
                dv.getVote()
        );
    }
}
