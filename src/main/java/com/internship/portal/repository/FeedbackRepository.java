package com.internship.portal.repository;

import com.internship.portal.entity.Feedback;
import com.internship.portal.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByUserAndFeedbackDate(User user, LocalDate date);

    @EntityGraph(attributePaths = "user")
    List<Feedback> findByUserOrderByFeedbackDateDesc(User user);

    @EntityGraph(attributePaths = "user")
    List<Feedback> findTop10ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "user")
    List<Feedback> findAllByOrderByFeedbackDateDesc();

    long countByFeedbackDate(LocalDate date);

    long countByUser(User user);

    void deleteByUser(User user);
}
