package com.deolhoneles.repository;

import com.deolhoneles.entity.LegislativeProposal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegislativeProposalRepository extends JpaRepository<LegislativeProposal, Long> {

    Optional<LegislativeProposal> findByExternalId(String externalId);
}
