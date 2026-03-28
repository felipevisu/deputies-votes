package com.deolhoneles.repository;

import com.deolhoneles.entity.Proposal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    Optional<Proposal> findByExternalId(Long externalId);

    @Query(value = """
            SELECT DISTINCT p FROM Proposal p
            JOIN ProposalAuthor pa ON pa.proposalId = p.id
            WHERE pa.deputyId IN :deputyIds
            ORDER BY p.presentationDate DESC, p.id ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p.id) FROM Proposal p
            JOIN ProposalAuthor pa ON pa.proposalId = p.id
            WHERE pa.deputyId IN :deputyIds
            """)
    Page<Proposal> findProposalsByAuthorDeputyIds(
            @Param("deputyIds") List<Long> deputyIds, Pageable pageable);
}
