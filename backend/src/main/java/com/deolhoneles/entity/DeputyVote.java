package com.deolhoneles.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "deputy_vote")
public class DeputyVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deputy_id", nullable = false)
    private Long deputyId;

    @Column(name = "proposal_id", nullable = false)
    private Long proposalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoteType vote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deputy_id", insertable = false, updatable = false)
    private Deputy deputy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", insertable = false, updatable = false)
    private LegislativeProposal proposal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeputyId() {
        return deputyId;
    }

    public void setDeputyId(Long deputyId) {
        this.deputyId = deputyId;
    }

    public Long getProposalId() {
        return proposalId;
    }

    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }

    public VoteType getVote() {
        return vote;
    }

    public void setVote(VoteType vote) {
        this.vote = vote;
    }

    public Deputy getDeputy() {
        return deputy;
    }

    public void setDeputy(Deputy deputy) {
        this.deputy = deputy;
    }

    public LegislativeProposal getProposal() {
        return proposal;
    }

    public void setProposal(LegislativeProposal proposal) {
        this.proposal = proposal;
    }
}
