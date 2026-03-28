package com.deolhoneles.service;

import com.deolhoneles.dto.VoteRequest;
import com.deolhoneles.dto.VoteResponse;
import com.deolhoneles.entity.DeputyVote;
import com.deolhoneles.repository.DeputyVoteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {

    private final DeputyVoteRepository deputyVoteRepository;

    public VoteService(DeputyVoteRepository deputyVoteRepository) {
        this.deputyVoteRepository = deputyVoteRepository;
    }

    @Transactional
    public VoteResponse createVote(VoteRequest request) {
        DeputyVote vote = new DeputyVote();
        vote.setDeputyId(request.deputyId());
        vote.setActivityId(request.activityId());
        vote.setVote(request.vote());
        try {
            deputyVoteRepository.save(vote);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Vote already exists for deputy " + request.deputyId()
                    + " on activity " + request.activityId());
        }
        return VoteResponse.from(vote);
    }
}
