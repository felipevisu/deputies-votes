package com.deolhoneles.repository;

import com.deolhoneles.entity.Deputy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeputyRepository extends JpaRepository<Deputy, Long> {

    Optional<Deputy> findByExternalId(Long externalId);
}
