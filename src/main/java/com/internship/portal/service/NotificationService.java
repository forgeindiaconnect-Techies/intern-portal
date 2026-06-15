package com.internship.portal.service;

import com.internship.portal.entity.Notification;
import com.internship.portal.entity.Role;
import com.internship.portal.entity.User;
import com.internship.portal.repository.NotificationRepository;
import com.internship.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void notifyUser(User user, String title, String message) {
        notifyUser(user, title, message, null);
    }

    public void notifyUser(User user, String title, String message, String targetUrl) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .targetUrl(targetUrl)
                .read(false)
                .build());
    }

    public void notifyAdmins(String title, String message) {
        notifyAdmins(title, message, null);
    }

    public void notifyAdmins(String title, String message, String targetUrl) {
        userRepository.findByRole(Role.ADMIN).forEach(admin ->
                notifyUser(admin, title, message, targetUrl));
    }

    public void broadcastToAll(String title, String message) {
        userRepository.findAll().forEach(user -> notifyUser(user, title, message));
    }

    public List<Notification> getNotificationsForUser(User user) {
        List<Notification> userNotifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        userNotifications.sort(Comparator.comparing(Notification::getCreatedAt).reversed());
        return userNotifications;
    }

    public List<Notification> getRecentNotifications() {
        return notificationRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public long countUnread(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markAsRead(Long id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (notification.getUser() != null && !notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public String markAsReadAndGetTargetUrl(Long id, User user, String fallbackUrl) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (notification.getUser() != null && !notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        String targetUrl = notification.getTargetUrl();
        if (targetUrl == null || targetUrl.isBlank() || !targetUrl.startsWith("/") || targetUrl.startsWith("//")) {
            return fallbackUrl;
        }
        return targetUrl;
    }

    public void markAllAsRead(User user) {
        getNotificationsForUser(user).forEach(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
