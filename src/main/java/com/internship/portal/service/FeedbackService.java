package com.internship.portal.service;

import com.internship.portal.entity.Attendance;
import com.internship.portal.entity.Feedback;
import com.internship.portal.entity.User;
import com.internship.portal.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AttendanceService attendanceService;
    private final NotificationService notificationService;

    public Optional<Feedback> getTodayFeedback(User user) {
        return feedbackRepository.findByUserAndFeedbackDate(user, attendanceService.getToday());
    }

    public Feedback submitFeedback(User user, String workDone, String dailyFeedback, String additionalNotes) {
        LocalDate today = attendanceService.getToday();

        Optional<Attendance> attendance = attendanceService.getTodayAttendance(user);
        if (attendance.isEmpty() || attendance.get().getCheckInTime() == null) {
            throw new IllegalStateException("Feedback allowed only after attendance check-in");
        }

        if (feedbackRepository.findByUserAndFeedbackDate(user, today).isPresent()) {
            throw new IllegalStateException("One feedback submission per day allowed");
        }

        Feedback feedback = Feedback.builder()
                .user(user)
                .feedbackDate(today)
                .workDone(workDone)
                .dailyFeedback(dailyFeedback)
                .additionalNotes(additionalNotes)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        notificationService.notifyUser(user, "Feedback Submitted", "Your daily feedback has been recorded.", "/student/feedback/history");
        notificationService.notifyAdmins("New Feedback", user.getFullName() + " submitted daily feedback.", "/admin/feedback#feedback-" + saved.getId());
        return saved;
    }

    public List<Feedback> getHistory(User user) {
        return feedbackRepository.findByUserOrderByFeedbackDateDesc(user);
    }

    public List<Feedback> getRecentFeedback() {
        return feedbackRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public long countSubmittedToday() {
        return feedbackRepository.countByFeedbackDate(attendanceService.getToday());
    }

    public long countByUser(User user) {
        return feedbackRepository.countByUser(user);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderByFeedbackDateDesc();
    }
}
