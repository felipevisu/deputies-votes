package com.deolhoneles.dto;

import jakarta.validation.constraints.NotNull;

public record EventDeputyRequest(
        @NotNull Long deputyId
) {
}
