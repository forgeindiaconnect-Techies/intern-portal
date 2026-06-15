package com.internship.portal.controller;

import com.internship.portal.entity.User;
import com.internship.portal.security.UserPrincipal;
import com.internship.portal.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentUserHelper currentUserHelper;

    @PostMapping("/check-in")
    public String checkIn(@AuthenticationPrincipal UserPrincipal principal,
                          @RequestParam(required = false) String photoData,
                          RedirectAttributes redirectAttributes) {
        User user = currentUserHelper.getCurrentUser(principal);
        try {
            attendanceService.checkIn(user, photoData);
            redirectAttributes.addFlashAttribute("success", "Checked in successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/attendance";
    }

    @PostMapping("/check-out")
    public String checkOut(@AuthenticationPrincipal UserPrincipal principal,
                           RedirectAttributes redirectAttributes) {
        User user = currentUserHelper.getCurrentUser(principal);
        try {
            attendanceService.checkOut(user);
            redirectAttributes.addFlashAttribute("success", "Checked out successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/attendance";
    }
}
