package com.deolhoneles.service;

import com.deolhoneles.dto.ActivityRequest;
import com.deolhoneles.dto.ActivityResponse;
import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.entity.LegislativeActivity;
import com.deolhoneles.repository.LegislativeActivityRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {

    private final LegislativeActivityRepository activityRepository;

    public ActivityService(LegislativeActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityResponse> listActivities(int page, int size) {
        Page<LegislativeActivity> result = activityRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "voteDate")));
        return PageResponse.from(result, result.getContent().stream()
                .map(ActivityResponse::from)
                .toList());
    }

    @Transactional(readOnly = true)
    public ActivityResponse getActivity(Long id) {
        LegislativeActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + id));
        return ActivityResponse.from(activity);
    }

    @Transactional(readOnly = true)
    public Optional<ActivityResponse> findByExternalId(String externalId) {
        return activityRepository.findByExternalId(externalId)
                .map(ActivityResponse::from);
    }

    @Transactional
    public ActivityResponse createActivity(ActivityRequest request) {
        LegislativeActivity activity = new LegislativeActivity();
        applyRequest(activity, request);
        activityRepository.save(activity);
        return ActivityResponse.from(activity);
    }

    @Transactional
    public ActivityResponse updateActivity(Long id, ActivityRequest request) {
        LegislativeActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + id));
        applyRequest(activity, request);
        activityRepository.save(activity);
        return ActivityResponse.from(activity);
    }

    @Transactional
    public void deleteActivity(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new RuntimeException("Activity not found: " + id);
        }
        activityRepository.deleteById(id);
    }

    @Transactional
    public void enrich(Long id, String subtitle, String summary, String sourceProposalId) {
        LegislativeActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + id));
        activity.setSubtitle(subtitle);
        activity.setSummary(summary);
        activity.setSourceProposalId(sourceProposalId);
        activityRepository.save(activity);
    }

    private void applyRequest(LegislativeActivity activity, ActivityRequest request) {
        activity.setTitle(request.title());
        activity.setSummary(request.summary());
        activity.setAuthor(request.author());
        activity.setCategory(request.category());
        activity.setVoteDate(request.voteDate());
        activity.setExternalId(request.externalId());
        activity.setDescription(request.description());
        activity.setVoteRound(request.voteRound());
    }
}
