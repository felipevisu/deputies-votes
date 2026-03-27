package com.deolhoneles.service;

import com.deolhoneles.dto.FeedItemResponse;
import com.deolhoneles.dto.FeedRequest;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.repository.DeputyVoteRepository;
import java.util.Collections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedService {

    private final DeputyVoteRepository deputyVoteRepository;

    public FeedService(DeputyVoteRepository deputyVoteRepository) {
        this.deputyVoteRepository = deputyVoteRepository;
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
}
