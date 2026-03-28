package com.deolhoneles.dto;

import com.deolhoneles.entity.VoteType;
import jakarta.validation.constraints.NotNull;

public record VoteRequest(
        @NotNull Long deputyId,
        @NotNull Long activityId,
        @NotNull VoteType vote
) {
}
