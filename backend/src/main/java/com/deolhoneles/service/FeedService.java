package com.deolhoneles.service;

import com.deolhoneles.dto.DeputyVoteSummary;
import com.deolhoneles.dto.FeedItemResponse;
import com.deolhoneles.dto.FeedRequest;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.dto.ProposalFeedItemResponse;
import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.entity.LegislativeProposal;
import com.deolhoneles.repository.DeputyVoteRepository;
import com.deolhoneles.repository.LegislativeProposalRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedService {

    private final DeputyVoteRepository deputyVoteRepository;
    private final LegislativeProposalRepository proposalRepository;

    public FeedService(DeputyVoteRepository deputyVoteRepository,
                       LegislativeProposalRepository proposalRepository) {
        this.deputyVoteRepository = deputyVoteRepository;
        this.proposalRepository = proposalRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedItemResponse> getFeed(FeedRequest request, int page, int size) {
        if (request.deputyIds() == null || request.deputyIds().isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0, 0, true);
        }

        PageRequest pageable = PageRequest.of(page, size);
        Page<DeputyVote> result = deputyVoteRepository.findFeedItemsByDeputyIds(request.deputyIds(), pageable);

        return PageResponse.from(result, result.getContent().stream()
                .map(FeedItemResponse::from)
                .toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<ProposalFeedItemResponse> getProposalFeed(FeedRequest request, int page, int size) {
        if (request.deputyIds() == null || request.deputyIds().isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0, 0, true);
        }

        PageRequest pageable = PageRequest.of(page, size);
        Page<LegislativeProposal> proposalPage =
                proposalRepository.findProposalsVotedByDeputyIds(request.deputyIds(), pageable);

        List<LegislativeProposal> proposals = proposalPage.getContent();
        if (proposals.isEmpty()) {
            return PageResponse.from(proposalPage, Collections.emptyList());
        }

        List<Long> proposalIds = proposals.stream().map(LegislativeProposal::getId).toList();
        List<DeputyVote> votes = deputyVoteRepository
                .findVotesByProposalIdsAndDeputyIds(proposalIds, request.deputyIds());

        Map<Long, List<DeputyVoteSummary>> votesByProposal = votes.stream()
                .collect(Collectors.groupingBy(
                        DeputyVote::getProposalId,
                        Collectors.mapping(DeputyVoteSummary::from, Collectors.toList())));

        List<ProposalFeedItemResponse> content = proposals.stream()
                .map(p -> new ProposalFeedItemResponse(
                        p.getId(),
                        p.getTitle(),
                        p.getSummary(),
                        p.getAuthor(),
                        p.getVoteDate(),
                        votesByProposal.getOrDefault(p.getId(), Collections.emptyList())))
                .toList();

        return PageResponse.from(proposalPage, content);
    }
}
