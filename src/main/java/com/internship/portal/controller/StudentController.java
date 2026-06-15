package com.internship.portal.controller;

import com.internship.portal.entity.Attendance;
import com.internship.portal.entity.Feedback;
import com.internship.portal.entity.User;
import com.internship.portal.security.UserPrincipal;
import com.internship.portal.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final UserService userService;
    private final AttendanceService attendanceService;
    private final FeedbackService feedbackService;
    private final NotificationService notificationService;
    private final QuoteService quoteService;
    private final CurrentUserHelper currentUserHelper;
    private final AttendanceWindowService attendanceWindowService;

    private void addCommonAttributes(Model model, User student) {
        model.addAttribute("user", student);
        model.addAttribute("unreadCount", notificationService.countUnread(student));
        model.addAttribute("attendanceWindow", attendanceWindowService.getWindow());
        model.addAttribute("attendanceWindowOpen", attendanceWindowService.isOpenNow());
        model.addAttribute("attendanceWindowMessage", attendanceWindowService.getClosedMessage());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User student = currentUserHelper.getCurrentUser(principal);
        Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(student);
        Optional<Feedback> todayFeedback = feedbackService.getTodayFeedback(student);

        boolean checkedIn = todayAttendance.map(a -> a.getCheckInTime() != null).orElse(false);
        boolean checkedOut = todayAttendance.map(a -> a.getCheckOutTime() != null).orElse(false);
        boolean feedbackSubmitted = todayFeedback.isPresent();

        String attendanceStatus = checkedOut ? "Checked Out" : checkedIn ? "Checked In" : "Not Marked";

        long presentDays = attendanceService.countPresentForUser(student);
        long absentDays = attendanceService.countAbsentForUser(student);
        long feedbackCount = feedbackService.countByUser(student);
        long totalDays = presentDays + absentDays;
        double percentage = totalDays > 0 ? Math.round((double) presentDays / totalDays * 100.0) : 0;

        addCommonAttributes(model, student);
        model.addAttribute("attendanceStatus", attendanceStatus);
        model.addAttribute("checkedIn", checkedIn);
        model.addAttribute("checkedOut", checkedOut);
        model.addAttribute("feedbackSubmitted", feedbackSubmitted);
        model.addAttribute("canSubmitFeedback", checkedIn && !feedbackSubmitted);
        model.addAttribute("quote", quoteService.getRandomQuote());
        model.addAttribute("todayAttendance", todayAttendance.orElse(null));
        model.addAttribute("presentDays", presentDays);
        model.addAttribute("absentDays", absentDays);
        model.addAttribute("feedbackCount", feedbackCount);
        model.addAttribute("attendancePercentage", percentage);
        return "student/dashboard";
    }

    @GetMapping("/attendance")
    public String attendance(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User student = currentUserHelper.getCurrentUser(principal);
        Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(student);
        addCommonAttributes(model, student);
        model.addAttribute("todayAttendance", todayAttendance.orElse(null));
        model.addAttribute("checkedIn", todayAttendance.map(a -> a.getCheckInTime() != null).orElse(false));
        model.addAttribute("checkedOut", todayAttendance.map(a -> a.getCheckOutTime() != null).orElse(false));
        return "student/attendance";
    }

    @GetMapping("/attendance/history")
    public String attendanceHistory(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User student = currentUserHelper.getCurrentUser(principal);
        List<Attendance> history = attendanceService.getHistory(student);
        long presentDays = attendanceService.countPresentForUser(student);
        long absentDays = attendanceService.countAbsentForUser(student);
        long totalDays = presentDays + absentDays;
        double percentage = totalDays > 0 ? Math.round((double) presentDays / totalDays * 100.0) : 0;

        addCommonAttributes(model, student);
        model.addAttribute("history", history);
        model.addAttribute("presentDays", presentDays);
        model.addAttribute("absentDays", absentDays);
        model.addAttribute("attendancePercentage", percentage);
        return "student/attendance-history";
    }

    @GetMapping("/feedback")
    public String feedback(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User student = currentUserHelper.getCurrentUser(principal);
        Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(student);
        boolean checkedIn = todayAttendance.map(a -> a.getCheckInTime() != null).orElse(false);
        boolean feedbackSubmitted = feedbackService.getTodayFeedback(student).isPresent();

        addCommonAttributes(model, student);
        model.addAttribute("checkedIn", checkedIn);
        model.addAttribute("feedbackSubmitted", feedbackSubmitted);
        return "student/feedback";
    }

    @GetMapping("/feedback/history")
    public String feedbackHistory(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User student = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, student);
        model.addAttribute("history", feedbackService.getHistory(student));
        return "student/feedback-history";
    }

    @GetMapping("/notifications")
    public String notifications(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User student = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, student);
        model.addAttribute("notifications", notificationService.getNotificationsForUser(student));
        return "student/notifications";
    }

    @GetMapping("/notifications/open/{id}")
    public String openNotification(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        String targetUrl = notificationService.markAsReadAndGetTargetUrl(
                id,
                currentUserHelper.getCurrentUser(principal),
                "/student/notifications");
        return "redirect:" + targetUrl;
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User student = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, student);
        return "student/profile";
    }

    @GetMapping("/settings")
    public String settings(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User student = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, student);
        return "student/settings";
    }
}
