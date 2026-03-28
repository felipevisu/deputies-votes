package com.deolhoneles.controller;

import com.deolhoneles.dto.ActivityFeedItemResponse;
import com.deolhoneles.dto.FeedItemResponse;
import com.deolhoneles.dto.FeedRequest;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.service.FeedService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @PostMapping("/deputies")
    public PageResponse<FeedItemResponse> getFeed(
            @RequestBody FeedRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return feedService.getFeed(request, page, size);
    }

    @PostMapping("/activities")
    public PageResponse<ActivityFeedItemResponse> getActivityFeed(
            @RequestBody FeedRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return feedService.getActivityFeed(request, page, size);
    }
}
