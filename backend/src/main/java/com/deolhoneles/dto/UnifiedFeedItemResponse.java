package com.deolhoneles.dto;

import com.deolhoneles.entity.LegislativeActivity;
import com.deolhoneles.entity.Proposal;
import java.time.LocalDate;
import java.util.List;

public record UnifiedFeedItemResponse(
        // Common
        String type,                          // 1
        LocalDate date,                       // 2
        // Voting (null when PROPOSAL)
        Long activityId,                      // 3
        String activityTitle,                 // 4
        String activitySubtitle,              // 5
        String activityVoteRound,             // 6
        String activitySummary,               // 7
        String activityAuthor,                // 8
        String activityCategory,              // 9
        List<DeputyVoteSummary> votes,        // 10
        // Proposal (null when VOTING)
        Long proposalId,                      // 11
        String proposalTypeCode,              // 12
        Integer proposalNumber,               // 13
        Integer proposalYear,                 // 14
        String proposalEmenta,                // 15
        String proposalStatus,                // 16
        List<ProposalAuthorSummary> authors   // 17
) {

    public static UnifiedFeedItemResponse fromActivity(
            LegislativeActivity a, List<DeputyVoteSummary> votes) {
        return new UnifiedFeedItemResponse(
                "VOTING",                       // 1
                a.getVoteDate(),                // 2
                a.getId(),                      // 3
                a.getTitle(),                   // 4
                a.getSubtitle(),                // 5
                a.getVoteRound(),               // 6
                a.getSummary(),                 // 7
                a.getAuthor(),                  // 8
                a.getCategory(),                // 9
                votes,                          // 10
                null,                           // 11
                null,                           // 12
                null,                           // 13
                null,                           // 14
                null,                           // 15
                null,                           // 16
                null                            // 17
        );
    }

    public static UnifiedFeedItemResponse fromProposal(
            Proposal p, List<ProposalAuthorSummary> authors) {
        return new UnifiedFeedItemResponse(
                "PROPOSAL",                     // 1
                p.getPresentationDate(),        // 2
                null,                           // 3
                null,                           // 4
                null,                           // 5
                null,                           // 6
                null,                           // 7
                null,                           // 8
                null,                           // 9
                null,                           // 10
                p.getId(),                      // 11
                p.getTypeCode(),                // 12
                p.getNumber(),                  // 13
                p.getYear(),                    // 14
                p.getEmenta(),                  // 15
                p.getStatusDescription(),       // 16
                authors                         // 17
        );
    }
}
