package com.deolhoneles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProposalRequest(
        @NotNull Long externalId,
        @NotBlank String typeCode,
        @NotNull Integer number,
        @NotNull Integer year,
        @NotBlank String ementa,
        String keywords,
        @NotNull LocalDate presentationDate,
        String statusDescription
) {
}
