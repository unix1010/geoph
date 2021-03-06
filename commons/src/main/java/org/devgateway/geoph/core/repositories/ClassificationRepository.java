package org.devgateway.geoph.core.repositories;

import org.devgateway.geoph.model.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author dbianco
 *         created on mar 03 2016.
 */
public interface ClassificationRepository extends JpaRepository<Classification, Long> {

    @Query("select a from Classification a where a.name = ?1")
    List<Classification> findByName(String name);
}
