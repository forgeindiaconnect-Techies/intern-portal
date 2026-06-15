package com.internship.portal.controller;

import com.internship.portal.entity.User;
import com.internship.portal.security.UserPrincipal;
import com.internship.portal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserHelper {

    private final UserService userService;

    public User getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
