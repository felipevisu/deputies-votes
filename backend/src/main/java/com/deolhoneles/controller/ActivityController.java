package com.deolhoneles.controller;

import com.deolhoneles.dto.ActivityRequest;
import com.deolhoneles.dto.ActivityResponse;
import com.deolhoneles.dto.EnrichRequest;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public PageResponse<ActivityResponse> listActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return activityService.listActivities(page, size);
    }

    @GetMapping("/{id}")
    public ActivityResponse getActivity(@PathVariable Long id) {
        return activityService.getActivity(id);
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<ActivityResponse> findByExternalId(@PathVariable String externalId) {
        return activityService.findByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse createActivity(@Valid @RequestBody ActivityRequest request) {
        return activityService.createActivity(request);
    }

    @PutMapping("/{id}")
    public ActivityResponse updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return activityService.updateActivity(id, request);
    }

    @PatchMapping("/{id}/enrich")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enrichActivity(@PathVariable Long id, @Valid @RequestBody EnrichRequest request) {
        activityService.enrich(id, request.subtitle(), request.summary(), request.sourceProposalId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
    }
}
