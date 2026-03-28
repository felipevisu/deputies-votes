package com.deolhoneles.controller;

import com.deolhoneles.dto.VoteRequest;
import com.deolhoneles.dto.VoteResponse;
import com.deolhoneles.service.VoteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse createVote(@Valid @RequestBody VoteRequest request) {
        return voteService.createVote(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<VoteResponse> createBatch(@Valid @RequestBody List<VoteRequest> requests) {
        return voteService.createBatch(requests);
    }
}
