package com.deolhoneles.service;

import com.deolhoneles.dto.ProposalAuthorRequest;
import com.deolhoneles.dto.ProposalRequest;
import com.deolhoneles.dto.ProposalResponse;
import com.deolhoneles.entity.Proposal;
import com.deolhoneles.entity.ProposalAuthor;
import com.deolhoneles.repository.ProposalAuthorRepository;
import com.deolhoneles.repository.ProposalRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProposalAuthorRepository proposalAuthorRepository;

    public ProposalService(ProposalRepository proposalRepository,
                           ProposalAuthorRepository proposalAuthorRepository) {
        this.proposalRepository = proposalRepository;
        this.proposalAuthorRepository = proposalAuthorRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ProposalResponse> findByExternalId(Long externalId) {
        return proposalRepository.findByExternalId(externalId)
                .map(ProposalResponse::from);
    }

    @Transactional
    public ProposalResponse createProposal(ProposalRequest request) {
        Proposal proposal = new Proposal();
        proposal.setExternalId(request.externalId());
        proposal.setTypeCode(request.typeCode());
        proposal.setNumber(request.number());
        proposal.setYear(request.year());
        proposal.setEmenta(request.ementa());
        proposal.setKeywords(request.keywords());
        proposal.setPresentationDate(request.presentationDate());
        proposal.setStatusDescription(request.statusDescription());
        proposalRepository.save(proposal);
        return ProposalResponse.from(proposal);
    }

    @Transactional
    public void addAuthor(Long proposalId, ProposalAuthorRequest request) {
        ProposalAuthor author = new ProposalAuthor();
        author.setProposalId(proposalId);
        author.setDeputyId(request.deputyId());
        author.setSigningOrder(request.signingOrder());
        author.setProponent(request.proponent());
        proposalAuthorRepository.save(author);
    }
}
