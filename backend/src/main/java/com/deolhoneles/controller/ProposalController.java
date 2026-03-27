package com.deolhoneles.controller;

import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.dto.ProposalRequest;
import com.deolhoneles.dto.ProposalResponse;
import com.deolhoneles.service.ProposalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/proposals")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @GetMapping
    public PageResponse<ProposalResponse> listProposals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return proposalService.listProposals(page, size);
    }

    @GetMapping("/{id}")
    public ProposalResponse getProposal(@PathVariable Long id) {
        return proposalService.getProposal(id);
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<ProposalResponse> findByExternalId(@PathVariable String externalId) {
        return proposalService.findByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProposalResponse createProposal(@Valid @RequestBody ProposalRequest request) {
        return proposalService.createProposal(request);
    }

    @PutMapping("/{id}")
    public ProposalResponse updateProposal(@PathVariable Long id, @Valid @RequestBody ProposalRequest request) {
        return proposalService.updateProposal(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProposal(@PathVariable Long id) {
        proposalService.deleteProposal(id);
    }
}
