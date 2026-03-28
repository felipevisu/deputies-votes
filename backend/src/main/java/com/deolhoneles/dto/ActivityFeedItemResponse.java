package com.deolhoneles.dto;

import java.time.LocalDate;
import java.util.List;

public record ActivityFeedItemResponse(
        Long activityId,
        String name,
        String description,
        String author,
        LocalDate voteDate,
        List<DeputyVoteSummary> votes
) {
}
