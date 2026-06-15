package com.internship.portal.controller;

import com.internship.portal.entity.User;
import com.internship.portal.security.UserPrincipal;
import com.internship.portal.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final CurrentUserHelper currentUserHelper;

    @PostMapping("/submit")
    public String submit(@AuthenticationPrincipal UserPrincipal principal,
                         @RequestParam String workDone,
                         @RequestParam String dailyFeedback,
                         @RequestParam(required = false) String additionalNotes,
                         RedirectAttributes redirectAttributes) {
        User user = currentUserHelper.getCurrentUser(principal);
        try {
            feedbackService.submitFeedback(user, workDone, dailyFeedback, additionalNotes);
            redirectAttributes.addFlashAttribute("success", "Feedback submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/feedback";
    }
}
