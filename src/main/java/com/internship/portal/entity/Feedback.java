package com.internship.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "feedback_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "feedback_date", nullable = false)
    private LocalDate feedbackDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String workDone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String dailyFeedback;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
