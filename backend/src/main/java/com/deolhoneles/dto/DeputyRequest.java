package com.deolhoneles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeputyRequest(
        @NotBlank String name,
        @NotBlank @Size(max = 50) String party,
        @NotBlank @Size(min = 2, max = 2) String state,
        String avatar,
        Long externalId
) {
}
