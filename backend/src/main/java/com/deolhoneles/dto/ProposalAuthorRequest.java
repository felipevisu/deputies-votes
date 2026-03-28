package com.deolhoneles.dto;

import jakarta.validation.constraints.NotNull;

public record ProposalAuthorRequest(
        @NotNull Long deputyId,
        int signingOrder,
        boolean proponent
) {
}
