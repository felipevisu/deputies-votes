package com.deolhoneles.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "proposal_author")
@IdClass(ProposalAuthorId.class)
public class ProposalAuthor {

    @Id
    @Column(name = "proposal_id", nullable = false)
    private Long proposalId;

    @Id
    @Column(name = "deputy_id", nullable = false)
    private Long deputyId;

    @Column(name = "signing_order", nullable = false)
    private Integer signingOrder;

    @Column(nullable = false)
    private Boolean proponent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", insertable = false, updatable = false)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deputy_id", insertable = false, updatable = false)
    private Deputy deputy;

    public Long getProposalId() {
        return proposalId;
    }

    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }

    public Long getDeputyId() {
        return deputyId;
    }

    public void setDeputyId(Long deputyId) {
        this.deputyId = deputyId;
    }

    public Integer getSigningOrder() {
        return signingOrder;
    }

    public void setSigningOrder(Integer signingOrder) {
        this.signingOrder = signingOrder;
    }

    public Boolean getProponent() {
        return proponent;
    }

    public void setProponent(Boolean proponent) {
        this.proponent = proponent;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    public Deputy getDeputy() {
        return deputy;
    }

    public void setDeputy(Deputy deputy) {
        this.deputy = deputy;
    }
}
