package com.deolhoneles.repository;

import com.deolhoneles.entity.Account;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    @Query("SELECT d.id FROM Account a JOIN a.followedDeputies d WHERE a.id = :accountId")
    Set<Long> findFollowedDeputyIds(@Param("accountId") Long accountId);
}
