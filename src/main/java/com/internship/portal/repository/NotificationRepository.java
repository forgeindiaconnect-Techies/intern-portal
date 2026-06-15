package com.internship.portal.repository;

import com.internship.portal.entity.Notification;
import com.internship.portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserIsNullOrderByCreatedAtDesc();

    List<Notification> findTop10ByOrderByCreatedAtDesc();

    long countByUserAndReadFalse(User user);

    void deleteByUser(User user);
}
