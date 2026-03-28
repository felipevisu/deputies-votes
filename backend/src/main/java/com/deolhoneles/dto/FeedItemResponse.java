package com.deolhoneles.dto;

import com.deolhoneles.entity.Deputy;
import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.entity.LegislativeActivity;
import com.deolhoneles.entity.VoteType;
import java.time.LocalDate;

public record FeedItemResponse(
        Long id,
        String name,
        String description,
        String deputieName,
        String deputieParty,
        String deputiePhoto,
        VoteType vote,
        String category,
        String author,
        LocalDate voteDate,
        Long deputyId
) {

    public static FeedItemResponse from(DeputyVote dv) {
        Deputy d = dv.getDeputy();
        LegislativeActivity a = dv.getActivity();
        return new FeedItemResponse(
                dv.getId(),
                a.getTitle(),
                a.getSummary(),
                d.getName(),
                d.getParty() + " - " + d.getState(),
                d.getAvatar(),
                dv.getVote(),
                a.getCategory(),
                a.getAuthor(),
                a.getVoteDate(),
                d.getId()
        );
    }
}
