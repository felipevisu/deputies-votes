package com.deolhoneles.controller;

import com.deolhoneles.dto.ProposalAuthorRequest;
import com.deolhoneles.dto.ProposalRequest;
import com.deolhoneles.dto.ProposalResponse;
import com.deolhoneles.service.ProposalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/proposals")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<ProposalResponse> findByExternalId(@PathVariable Long externalId) {
        return proposalService.findByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProposalResponse createProposal(@Valid @RequestBody ProposalRequest request) {
        return proposalService.createProposal(request);
    }

    @PostMapping("/{id}/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAuthor(@PathVariable Long id, @Valid @RequestBody ProposalAuthorRequest request) {
        proposalService.addAuthor(id, request);
    }
}
