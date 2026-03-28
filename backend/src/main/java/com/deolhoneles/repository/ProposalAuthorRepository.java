package com.deolhoneles.repository;

import com.deolhoneles.entity.ProposalAuthor;
import com.deolhoneles.entity.ProposalAuthorId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProposalAuthorRepository extends JpaRepository<ProposalAuthor, ProposalAuthorId> {

    @Query("""
            SELECT pa FROM ProposalAuthor pa
            JOIN FETCH pa.deputy d
            WHERE pa.proposalId IN :proposalIds
            AND pa.deputyId IN :deputyIds
            ORDER BY pa.signingOrder ASC
            """)
    List<ProposalAuthor> findAuthorsByProposalIdsAndDeputyIds(
            @Param("proposalIds") List<Long> proposalIds,
            @Param("deputyIds") List<Long> deputyIds);
}
