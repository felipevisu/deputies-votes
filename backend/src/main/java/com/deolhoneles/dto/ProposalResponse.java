package com.deolhoneles.dto;

import com.deolhoneles.entity.Proposal;
import java.time.LocalDate;

public record ProposalResponse(
        Long id,
        Long externalId,
        String typeCode,
        Integer number,
        Integer year,
        String ementa,
        String keywords,
        LocalDate presentationDate,
        String statusDescription
) {

    public static ProposalResponse from(Proposal entity) {
        return new ProposalResponse(
                entity.getId(),
                entity.getExternalId(),
                entity.getTypeCode(),
                entity.getNumber(),
                entity.getYear(),
                entity.getEmenta(),
                entity.getKeywords(),
                entity.getPresentationDate(),
                entity.getStatusDescription()
        );
    }
}
