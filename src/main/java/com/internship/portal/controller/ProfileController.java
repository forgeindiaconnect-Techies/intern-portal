package com.internship.portal.controller;

import com.internship.portal.entity.User;
import com.internship.portal.security.UserPrincipal;
import com.internship.portal.service.NotificationService;
import com.internship.portal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final CurrentUserHelper currentUserHelper;

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        User user = currentUserHelper.getCurrentUser(principal);
        try {
            userService.updateProfile(user, firstName, lastName, email, phone);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        String redirect = user.getRole().name().equals("ADMIN") ? "/admin/profile" : "/student/profile";
        return "redirect:" + redirect;
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = currentUserHelper.getCurrentUser(principal);
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
        } else {
            try {
                userService.changePassword(user, currentPassword, newPassword);
                redirectAttributes.addFlashAttribute("success", "Password changed successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
        }
        String redirect = user.getRole().name().equals("ADMIN") ? "/admin/profile" : "/student/profile";
        return "redirect:" + redirect;
    }

    @PostMapping("/upload-photo")
    public String uploadPhoto(@AuthenticationPrincipal UserPrincipal principal,
                              @RequestParam MultipartFile photo,
                              RedirectAttributes redirectAttributes) {
        User user = currentUserHelper.getCurrentUser(principal);
        try {
            userService.saveProfilePicture(user, photo);
            redirectAttributes.addFlashAttribute("success", "Profile picture uploaded");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        String redirect = user.getRole().name().equals("ADMIN") ? "/admin/profile" : "/student/profile";
        return "redirect:" + redirect;
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        User user = currentUserHelper.getCurrentUser(principal);
        notificationService.markAllAsRead(user);
        String redirect = user.getRole().name().equals("ADMIN") ? "/admin/notifications" : "/student/notifications";
        return "redirect:" + redirect;
    }
}
