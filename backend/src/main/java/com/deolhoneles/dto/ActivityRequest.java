package com.deolhoneles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ActivityRequest(
        @NotBlank String title,
        @NotBlank String summary,
        @NotBlank String author,
        @NotBlank String category,
        @NotNull LocalDate voteDate,
        String externalId,
        String voteRound
) {
}
