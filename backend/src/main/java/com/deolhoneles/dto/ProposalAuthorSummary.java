package com.deolhoneles.dto;

import com.deolhoneles.entity.Deputy;
import com.deolhoneles.entity.ProposalAuthor;

public record ProposalAuthorSummary(
        Long deputyId,
        String name,
        String party,
        String state,
        String photo,
        int signingOrder,
        boolean proponent
) {

    public static ProposalAuthorSummary from(ProposalAuthor pa) {
        Deputy d = pa.getDeputy();
        return new ProposalAuthorSummary(
                d.getId(),
                d.getName(),
                d.getParty(),
                d.getState(),
                d.getAvatar(),
                pa.getSigningOrder(),
                pa.getProponent()
        );
    }
}
