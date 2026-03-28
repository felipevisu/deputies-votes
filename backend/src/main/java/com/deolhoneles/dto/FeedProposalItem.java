package com.deolhoneles.dto;

import com.deolhoneles.entity.Proposal;
import java.util.List;

public record FeedProposalItem(
        Long id,
        Long externalId,
        String typeCode,
        Integer number,
        Integer year,
        String ementa,
        String status,
        List<ProposalAuthorSummary> authors
) {

    public static FeedProposalItem from(Proposal p, List<ProposalAuthorSummary> authors) {
        return new FeedProposalItem(
                p.getId(),
                p.getExternalId(),
                p.getTypeCode(),
                p.getNumber(),
                p.getYear(),
                p.getEmenta(),
                p.getStatusDescription(),
                authors
        );
    }
}
