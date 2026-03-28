package com.deolhoneles.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProposalAuthorId implements Serializable {

    private Long proposalId;
    private Long deputyId;

    public ProposalAuthorId() {
    }

    public ProposalAuthorId(Long proposalId, Long deputyId) {
        this.proposalId = proposalId;
        this.deputyId = deputyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProposalAuthorId that = (ProposalAuthorId) o;
        return Objects.equals(proposalId, that.proposalId)
                && Objects.equals(deputyId, that.deputyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proposalId, deputyId);
    }
}
