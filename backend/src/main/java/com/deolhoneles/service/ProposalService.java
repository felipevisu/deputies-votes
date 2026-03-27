package com.deolhoneles.service;

import com.deolhoneles.dto.PageResponse;
import com.deolhoneles.dto.ProposalRequest;
import com.deolhoneles.dto.ProposalResponse;
import com.deolhoneles.entity.LegislativeProposal;
import com.deolhoneles.repository.LegislativeProposalRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProposalService {

    private final LegislativeProposalRepository proposalRepository;

    public ProposalService(LegislativeProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProposalResponse> listProposals(int page, int size) {
        Page<LegislativeProposal> result = proposalRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "voteDate")));
        return PageResponse.from(result, result.getContent().stream()
                .map(ProposalResponse::from)
                .toList());
    }

    @Transactional(readOnly = true)
    public ProposalResponse getProposal(Long id) {
        LegislativeProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + id));
        return ProposalResponse.from(proposal);
    }

    @Transactional(readOnly = true)
    public Optional<ProposalResponse> findByExternalId(String externalId) {
        return proposalRepository.findByExternalId(externalId)
                .map(ProposalResponse::from);
    }

    @Transactional
    public ProposalResponse createProposal(ProposalRequest request) {
        LegislativeProposal proposal = new LegislativeProposal();
        applyRequest(proposal, request);
        proposalRepository.save(proposal);
        return ProposalResponse.from(proposal);
    }

    @Transactional
    public ProposalResponse updateProposal(Long id, ProposalRequest request) {
        LegislativeProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + id));
        applyRequest(proposal, request);
        proposalRepository.save(proposal);
        return ProposalResponse.from(proposal);
    }

    @Transactional
    public void deleteProposal(Long id) {
        if (!proposalRepository.existsById(id)) {
            throw new RuntimeException("Proposal not found: " + id);
        }
        proposalRepository.deleteById(id);
    }

    private void applyRequest(LegislativeProposal proposal, ProposalRequest request) {
        proposal.setTitle(request.title());
        proposal.setSummary(request.summary());
        proposal.setAuthor(request.author());
        proposal.setCategory(request.category());
        proposal.setVoteDate(request.voteDate());
        proposal.setExternalId(request.externalId());
    }
}
