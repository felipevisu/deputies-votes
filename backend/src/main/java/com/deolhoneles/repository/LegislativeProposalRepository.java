package com.deolhoneles.repository;

import com.deolhoneles.entity.LegislativeProposal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LegislativeProposalRepository extends JpaRepository<LegislativeProposal, Long> {

    Optional<LegislativeProposal> findByExternalId(String externalId);

    @Query(value = """
            SELECT DISTINCT p FROM LegislativeProposal p
            JOIN DeputyVote dv ON dv.proposalId = p.id
            WHERE dv.deputyId IN :deputyIds
            ORDER BY p.voteDate DESC, p.id ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p.id) FROM LegislativeProposal p
            JOIN DeputyVote dv ON dv.proposalId = p.id
            WHERE dv.deputyId IN :deputyIds
            """)
    Page<LegislativeProposal> findProposalsVotedByDeputyIds(
            @Param("deputyIds") List<Long> deputyIds, Pageable pageable);
}
