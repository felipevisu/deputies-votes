package com.deolhoneles.service;

import com.deolhoneles.dto.DeputyVoteSummary;
import com.deolhoneles.dto.FeedRequest;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.dto.ProposalAuthorSummary;
import com.deolhoneles.dto.UnifiedFeedItemResponse;
import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.entity.LegislativeActivity;
import com.deolhoneles.entity.Proposal;
import com.deolhoneles.entity.ProposalAuthor;
import com.deolhoneles.repository.DeputyVoteRepository;
import com.deolhoneles.repository.LegislativeActivityRepository;
import com.deolhoneles.repository.ProposalAuthorRepository;
import com.deolhoneles.repository.ProposalRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final LegislativeActivityRepository activityRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalAuthorRepository proposalAuthorRepository;

    public FeedService(DeputyVoteRepository deputyVoteRepository,
                       LegislativeActivityRepository activityRepository,
                       ProposalRepository proposalRepository,
                       ProposalAuthorRepository proposalAuthorRepository) {
        this.deputyVoteRepository = deputyVoteRepository;
        this.activityRepository = activityRepository;
        this.proposalRepository = proposalRepository;
        this.proposalAuthorRepository = proposalAuthorRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UnifiedFeedItemResponse> getUnifiedFeed(FeedRequest request, int page, int size) {
        if (request.deputyIds() == null || request.deputyIds().isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0, 0, true);
        }

        List<Long> deputyIds = request.deputyIds();
        int needed = (page + 1) * size;

        // Fetch enough from both sources to cover the requested page
        Page<LegislativeActivity> activityPage =
                activityRepository.findActivitiesVotedByDeputyIds(deputyIds, PageRequest.of(0, needed));
        Page<Proposal> proposalPage =
                proposalRepository.findProposalsByAuthorDeputyIds(deputyIds, PageRequest.of(0, needed));

        long totalElements = activityPage.getTotalElements() + proposalPage.getTotalElements();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Merge both lists by date descending
        List<Object> merged = new ArrayList<>();
        merged.addAll(activityPage.getContent());
        merged.addAll(proposalPage.getContent());
        merged.sort(Comparator.comparing(this::extractDate).reversed()
                .thenComparing(item -> item instanceof LegislativeActivity ? 0 : 1));

        // Slice to requested page
        int start = page * size;
        int end = Math.min(start + size, merged.size());
        if (start >= merged.size()) {
            boolean isLast = page >= totalPages - 1 || totalElements == 0;
            return new PageResponse<>(Collections.emptyList(), page, size, totalElements, totalPages, isLast);
        }
        List<Object> pageItems = merged.subList(start, end);

        // Separate activities and proposals from the page slice
        List<LegislativeActivity> activities = pageItems.stream()
                .filter(LegislativeActivity.class::isInstance)
                .map(LegislativeActivity.class::cast)
                .toList();
        List<Proposal> proposals = pageItems.stream()
                .filter(Proposal.class::isInstance)
                .map(Proposal.class::cast)
                .toList();

        // Batch-load votes for activities
        Map<Long, List<DeputyVoteSummary>> votesByActivity = Collections.emptyMap();
        if (!activities.isEmpty()) {
            List<Long> activityIds = activities.stream().map(LegislativeActivity::getId).toList();
            List<DeputyVote> votes = deputyVoteRepository
                    .findVotesByActivityIdsAndDeputyIds(activityIds, deputyIds);
            votesByActivity = votes.stream()
                    .collect(Collectors.groupingBy(
                            DeputyVote::getActivityId,
                            Collectors.mapping(DeputyVoteSummary::from, Collectors.toList())));
        }

        // Batch-load authors for proposals
        Map<Long, List<ProposalAuthorSummary>> authorsByProposal = Collections.emptyMap();
        if (!proposals.isEmpty()) {
            List<Long> proposalIds = proposals.stream().map(Proposal::getId).toList();
            List<ProposalAuthor> authors = proposalAuthorRepository
                    .findAuthorsByProposalIdsAndDeputyIds(proposalIds, deputyIds);
            authorsByProposal = authors.stream()
                    .collect(Collectors.groupingBy(
                            ProposalAuthor::getProposalId,
                            Collectors.mapping(ProposalAuthorSummary::from, Collectors.toList())));
        }

        // Build response items preserving merged order
        Map<Long, List<DeputyVoteSummary>> finalVotes = votesByActivity;
        Map<Long, List<ProposalAuthorSummary>> finalAuthors = authorsByProposal;
        List<UnifiedFeedItemResponse> content = pageItems.stream()
                .map(item -> {
                    if (item instanceof LegislativeActivity a) {
                        return UnifiedFeedItemResponse.fromActivity(a,
                                finalVotes.getOrDefault(a.getId(), Collections.emptyList()));
                    } else {
                        Proposal p = (Proposal) item;
                        return UnifiedFeedItemResponse.fromProposal(p,
                                finalAuthors.getOrDefault(p.getId(), Collections.emptyList()));
                    }
                })
                .toList();

        boolean isLast = page >= totalPages - 1 || totalElements == 0;
        return new PageResponse<>(content, page, size, totalElements, totalPages, isLast);
    }

    private LocalDate extractDate(Object item) {
        if (item instanceof LegislativeActivity a) {
            return a.getVoteDate();
        }
        return ((Proposal) item).getPresentationDate();
    }
}
