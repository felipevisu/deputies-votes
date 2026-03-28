package com.deolhoneles.dto;

import jakarta.validation.constraints.NotBlank;

public record EnrichRequest(
        @NotBlank String subtitle,
        @NotBlank String summary,
        String sourceProposalId
) {
}
