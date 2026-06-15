package com.internship.portal.service;

import com.internship.portal.entity.Role;
import com.internship.portal.entity.User;
import com.internship.portal.repository.AttendanceRepository;
import com.internship.portal.repository.FeedbackRepository;
import com.internship.portal.repository.NotificationRepository;
import com.internship.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final String uploadDir;

    public UserService(UserRepository userRepository,
                       AttendanceRepository attendanceRepository,
                       FeedbackRepository feedbackRepository,
                       NotificationRepository notificationRepository,
                       PasswordEncoder passwordEncoder,
                       NotificationService notificationService,
                       @Value("${app.upload.dir:uploads/profiles}") String uploadDir) {
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.feedbackRepository = feedbackRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.uploadDir = uploadDir;
    }

    public List<User> getAllStudents() {
        return userRepository.findByRole(Role.STUDENT);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public long countActiveStudents() {
        return userRepository.countByRoleAndActive(Role.STUDENT, true);
    }

    public long countAllStudents() {
        return userRepository.countByRole(Role.STUDENT);
    }

    public User createStudent(User user, String rawPassword) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.setRole(Role.STUDENT);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        User saved = userRepository.save(user);
        notificationService.notifyUser(saved, "Welcome!", "Your internship portal account has been created. Login with your credentials.", "/student/dashboard");
        return saved;
    }

    public User updateStudent(Long id, User updated) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (!user.getUsername().equals(updated.getUsername()) && userRepository.existsByUsername(updated.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (!user.getEmail().equals(updated.getEmail()) && userRepository.existsByEmail(updated.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.setUsername(updated.getUsername());
        user.setEmail(updated.getEmail());
        user.setFirstName(updated.getFirstName());
        user.setLastName(updated.getLastName());
        user.setPhone(updated.getPhone());
        user.setCollege(updated.getCollege());
        user.setDepartment(updated.getDepartment());
        user.setYear(updated.getYear());
        return userRepository.save(user);
    }

    public void deleteStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Cannot delete non-student user");
        }
        attendanceRepository.deleteByUser(user);
        feedbackRepository.deleteByUser(user);
        notificationRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    public void setActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        user.setActive(active);
        userRepository.save(user);
        String status = active ? "activated" : "deactivated";
        notificationService.notifyUser(user, "Account " + status, "Your account has been " + status + " by admin.", "/student/profile");
    }

    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        notificationService.notifyUser(user, "Password Reset", "Your password has been reset by admin.", "/student/profile");
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User updateProfile(User user, String firstName, String lastName, String email, String phone) {
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        return userRepository.save(user);
    }

    public String saveProfilePicture(User user, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file");
        }
        String original = file.getOriginalFilename();
        String extension = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.')) : ".jpg";
        String filename = UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path target = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        user.setProfilePicture("/uploads/" + filename);
        userRepository.save(user);
        return user.getProfilePicture();
    }
}
