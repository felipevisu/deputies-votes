package com.deolhoneles.dto;

import com.deolhoneles.entity.LegislativeActivity;
import java.util.List;

public record FeedActivityItem(
        Long id,
        String externalId,
        String title,
        String subtitle,
        String description,
        String voteRound,
        String summary,
        String author,
        String category,
        List<DeputyVoteSummary> votes
) {

    public static FeedActivityItem from(LegislativeActivity a, List<DeputyVoteSummary> votes) {
        return new FeedActivityItem(
                a.getId(),
                a.getExternalId(),
                a.getTitle(),
                a.getSubtitle(),
                a.getDescription(),
                a.getVoteRound(),
                a.getSummary(),
                a.getAuthor(),
                a.getCategory(),
                votes
        );
    }
}
