package com.internship.portal.repository;

import com.internship.portal.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByActiveTrue();

    @Query(value = "SELECT * FROM quotes WHERE active = true ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Quote> findRandomActiveQuote();
}
