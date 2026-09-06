package com.tenet.urlshortener.repository;

import com.tenet.urlshortener.model.UrlMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortKey(String shortKey);

    boolean existsByShortKey(String shortKey);

    @Query("SELECT u FROM UrlMapping u WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.originalUrl) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.shortKey) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UrlMapping> searchMappings(@Param("search") String search, Pageable pageable);
}
