package com.deolhoneles.service;

import com.deolhoneles.dto.ActivityFeedItemResponse;
import com.deolhoneles.dto.DeputyVoteSummary;
import com.deolhoneles.dto.FeedItemResponse;
import com.deolhoneles.dto.FeedRequest;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.entity.LegislativeActivity;
import com.deolhoneles.repository.DeputyVoteRepository;
import com.deolhoneles.repository.LegislativeActivityRepository;
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
    private final LegislativeActivityRepository activityRepository;

    public FeedService(DeputyVoteRepository deputyVoteRepository,
                       LegislativeActivityRepository activityRepository) {
        this.deputyVoteRepository = deputyVoteRepository;
        this.activityRepository = activityRepository;
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
    public PageResponse<ActivityFeedItemResponse> getActivityFeed(FeedRequest request, int page, int size) {
        if (request.deputyIds() == null || request.deputyIds().isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0, 0, true);
        }

        PageRequest pageable = PageRequest.of(page, size);
        Page<LegislativeActivity> activityPage =
                activityRepository.findActivitiesVotedByDeputyIds(request.deputyIds(), pageable);

        List<LegislativeActivity> activities = activityPage.getContent();
        if (activities.isEmpty()) {
            return PageResponse.from(activityPage, Collections.emptyList());
        }

        List<Long> activityIds = activities.stream().map(LegislativeActivity::getId).toList();
        List<DeputyVote> votes = deputyVoteRepository
                .findVotesByActivityIdsAndDeputyIds(activityIds, request.deputyIds());

        Map<Long, List<DeputyVoteSummary>> votesByActivity = votes.stream()
                .collect(Collectors.groupingBy(
                        DeputyVote::getActivityId,
                        Collectors.mapping(DeputyVoteSummary::from, Collectors.toList())));

        List<ActivityFeedItemResponse> content = activities.stream()
                .map(a -> new ActivityFeedItemResponse(
                        a.getId(),
                        a.getTitle(),
                        a.getSummary(),
                        a.getAuthor(),
                        a.getVoteDate(),
                        votesByActivity.getOrDefault(a.getId(), Collections.emptyList())))
                .toList();

        return PageResponse.from(activityPage, content);
    }
}
