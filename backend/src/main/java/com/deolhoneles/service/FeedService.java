package com.deolhoneles.service;

import com.deolhoneles.dto.DeputyVoteSummary;
import com.deolhoneles.dto.EventDeputySummary;
import com.deolhoneles.dto.FeedRequest;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.dto.ProposalAuthorSummary;
import com.deolhoneles.dto.UnifiedFeedItemResponse;
import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.entity.Event;
import com.deolhoneles.entity.EventDeputy;
import com.deolhoneles.entity.LegislativeActivity;
import com.deolhoneles.entity.Proposal;
import com.deolhoneles.entity.ProposalAuthor;
import com.deolhoneles.entity.Deputy;
import com.deolhoneles.entity.VoteType;
import com.deolhoneles.repository.DeputyRepository;
import com.deolhoneles.repository.DeputyVoteRepository;
import com.deolhoneles.repository.EventDeputyRepository;
import com.deolhoneles.repository.EventRepository;
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

    private final DeputyRepository deputyRepository;
    private final DeputyVoteRepository deputyVoteRepository;
    private final LegislativeActivityRepository activityRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalAuthorRepository proposalAuthorRepository;
    private final EventRepository eventRepository;
    private final EventDeputyRepository eventDeputyRepository;

    public FeedService(DeputyRepository deputyRepository,
                       DeputyVoteRepository deputyVoteRepository,
                       LegislativeActivityRepository activityRepository,
                       ProposalRepository proposalRepository,
                       ProposalAuthorRepository proposalAuthorRepository,
                       EventRepository eventRepository,
                       EventDeputyRepository eventDeputyRepository) {
        this.deputyRepository = deputyRepository;
        this.deputyVoteRepository = deputyVoteRepository;
        this.activityRepository = activityRepository;
        this.proposalRepository = proposalRepository;
        this.proposalAuthorRepository = proposalAuthorRepository;
        this.eventRepository = eventRepository;
        this.eventDeputyRepository = eventDeputyRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UnifiedFeedItemResponse> getUnifiedFeed(FeedRequest request, int page, int size) {
        if (request.deputyIds() == null || request.deputyIds().isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0, 0, true);
        }

        List<Long> deputyIds = request.deputyIds();
        int needed = (page + 1) * size;

        // Fetch enough from all sources to cover the requested page
        Page<LegislativeActivity> activityPage =
                activityRepository.findActivitiesVotedByDeputyIds(deputyIds, PageRequest.of(0, needed));
        Page<Proposal> proposalPage =
                proposalRepository.findProposalsByAuthorDeputyIds(deputyIds, PageRequest.of(0, needed));
        Page<Event> eventPage =
                eventRepository.findEventsByDeputyIds(deputyIds, PageRequest.of(0, needed));

        long totalElements = activityPage.getTotalElements() + proposalPage.getTotalElements()
                + eventPage.getTotalElements();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Merge all lists by date descending
        List<Object> merged = new ArrayList<>();
        merged.addAll(activityPage.getContent());
        merged.addAll(proposalPage.getContent());
        merged.addAll(eventPage.getContent());
        merged.sort(Comparator.comparing(this::extractDate).reversed()
                .thenComparing(item -> {
                    if (item instanceof LegislativeActivity) return 0;
                    if (item instanceof Proposal) return 1;
                    return 2;
                }));

        // Slice to requested page
        int start = page * size;
        int end = Math.min(start + size, merged.size());
        if (start >= merged.size()) {
            boolean isLast = page >= totalPages - 1 || totalElements == 0;
            return new PageResponse<>(Collections.emptyList(), page, size, totalElements, totalPages, isLast);
        }
        List<Object> pageItems = merged.subList(start, end);

        // Separate activities, proposals and events from the page slice
        List<LegislativeActivity> activities = pageItems.stream()
                .filter(LegislativeActivity.class::isInstance)
                .map(LegislativeActivity.class::cast)
                .toList();
        List<Proposal> proposals = pageItems.stream()
                .filter(Proposal.class::isInstance)
                .map(Proposal.class::cast)
                .toList();
        List<Event> events = pageItems.stream()
                .filter(Event.class::isInstance)
                .map(Event.class::cast)
                .toList();

        // Batch-load votes for activities, adding absent deputies
        Map<Long, List<DeputyVoteSummary>> votesByActivity = Collections.emptyMap();
        if (!activities.isEmpty()) {
            List<Long> activityIds = activities.stream().map(LegislativeActivity::getId).toList();
            List<DeputyVote> votes = deputyVoteRepository
                    .findVotesByActivityIdsAndDeputyIds(activityIds, deputyIds);
            Map<Long, List<DeputyVoteSummary>> grouped = votes.stream()
                    .collect(Collectors.groupingBy(
                            DeputyVote::getActivityId,
                            Collectors.mapping(DeputyVoteSummary::from, Collectors.toList())));

            List<Deputy> followedDeputies = deputyRepository.findAllById(deputyIds);
            Map<Long, Deputy> deputyMap = followedDeputies.stream()
                    .collect(Collectors.toMap(Deputy::getId, d -> d));

            Map<Long, String> categoryById = activities.stream()
                    .collect(Collectors.toMap(LegislativeActivity::getId, a -> a.getCategory() != null ? a.getCategory() : ""));

            votesByActivity = new java.util.HashMap<>(grouped);
            for (Long activityId : activityIds) {
                if (!"Plenario".equals(categoryById.get(activityId))) {
                    continue;
                }
                List<DeputyVoteSummary> existing = votesByActivity
                        .computeIfAbsent(activityId, k -> new ArrayList<>());
                java.util.Set<Long> votedIds = existing.stream()
                        .map(DeputyVoteSummary::deputyId)
                        .collect(Collectors.toSet());
                for (Long did : deputyIds) {
                    if (!votedIds.contains(did) && deputyMap.containsKey(did)) {
                        Deputy d = deputyMap.get(did);
                        existing.add(new DeputyVoteSummary(
                                d.getId(), d.getName(), d.getParty(),
                                d.getState(), d.getAvatar(), VoteType.AUSENTE));
                    }
                }
            }
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

        // Batch-load deputies for events
        Map<Long, List<EventDeputySummary>> deputiesByEvent = Collections.emptyMap();
        if (!events.isEmpty()) {
            List<Long> eventIds = events.stream().map(Event::getId).toList();
            List<EventDeputy> eventDeputies = eventDeputyRepository
                    .findByEventIdsAndDeputyIds(eventIds, deputyIds);
            deputiesByEvent = eventDeputies.stream()
                    .collect(Collectors.groupingBy(
                            EventDeputy::getEventId,
                            Collectors.mapping(ed -> EventDeputySummary.from(ed.getDeputy()),
                                    Collectors.toList())));
        }

        // Build response items preserving merged order
        Map<Long, List<DeputyVoteSummary>> finalVotes = votesByActivity;
        Map<Long, List<ProposalAuthorSummary>> finalAuthors = authorsByProposal;
        Map<Long, List<EventDeputySummary>> finalDeputies = deputiesByEvent;
        List<UnifiedFeedItemResponse> content = pageItems.stream()
                .map(item -> {
                    if (item instanceof LegislativeActivity a) {
                        return UnifiedFeedItemResponse.fromActivity(a,
                                finalVotes.getOrDefault(a.getId(), Collections.emptyList()));
                    } else if (item instanceof Proposal p) {
                        return UnifiedFeedItemResponse.fromProposal(p,
                                finalAuthors.getOrDefault(p.getId(), Collections.emptyList()));
                    } else {
                        Event e = (Event) item;
                        return UnifiedFeedItemResponse.fromEvent(e,
                                finalDeputies.getOrDefault(e.getId(), Collections.emptyList()));
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
        if (item instanceof Proposal p) {
            return p.getPresentationDate();
        }
        return ((Event) item).getEventDate();
    }
}
