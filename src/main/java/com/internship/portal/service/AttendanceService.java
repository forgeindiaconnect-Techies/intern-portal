package com.internship.portal.service;

import com.internship.portal.entity.Attendance;
import com.internship.portal.entity.AttendanceStatus;
import com.internship.portal.entity.User;
import com.internship.portal.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Base64;

@Service
@Transactional
public class AttendanceService {

    private static final ZoneId ATTENDANCE_ZONE = ZoneId.of("Asia/Kolkata");

    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;
    private final AttendanceWindowService attendanceWindowService;
    private final String uploadDir;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             NotificationService notificationService,
                             AttendanceWindowService attendanceWindowService,
                             @Value("${app.upload.dir:uploads/profiles}") String uploadDir) {
        this.attendanceRepository = attendanceRepository;
        this.notificationService = notificationService;
        this.attendanceWindowService = attendanceWindowService;
        this.uploadDir = uploadDir;
    }

    public Optional<Attendance> getTodayAttendance(User user) {
        return attendanceRepository.findByUserAndAttendanceDate(user, getToday());
    }

    public Attendance checkIn(User user, MultipartFile photo) throws IOException {
        return checkIn(user, photo, null);
    }

    public Attendance checkIn(User user, String photoData) throws IOException {
        return checkIn(user, null, photoData);
    }

    private Attendance checkIn(User user, MultipartFile photo, String photoData) throws IOException {
        if (!attendanceWindowService.isOpenNow()) {
            throw new IllegalStateException(attendanceWindowService.getClosedMessage());
        }

        LocalDate today = getToday();
        Optional<Attendance> existing = attendanceRepository.findByUserAndAttendanceDate(user, today);
        if (existing.isPresent() && existing.get().getCheckInTime() != null) {
            throw new IllegalStateException("Already checked in today");
        }

        Attendance attendance = existing.orElse(Attendance.builder()
                .user(user)
                .attendanceDate(today)
                .status(AttendanceStatus.ABSENT)
                .build());

        attendance.setCheckInTime(getCurrentTime());
        attendance.setStatus(AttendanceStatus.PRESENT);

        if (photoData != null && !photoData.isBlank()) {
            String photoPath = saveLivePhoto(photoData, user.getUsername());
            attendance.setAttendancePhoto(photoPath);
        } else if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(photo, user.getUsername());
            attendance.setAttendancePhoto(photoPath);
        } else {
            throw new IllegalStateException("Please capture a live photo before checking in");
        }

        Attendance saved = attendanceRepository.save(attendance);
        notificationService.notifyUser(user, "Check-In Successful", "You checked in at " + saved.getCheckInTime(), "/student/attendance");
        notificationService.notifyAdmins("Student Check-In", user.getFullName() + " checked in at " + saved.getCheckInTime(), "/admin/attendance");
        return saved;
    }

    public Attendance checkOut(User user) {
        LocalDate today = getToday();
        Attendance attendance = attendanceRepository.findByUserAndAttendanceDate(user, today)
                .orElseThrow(() -> new IllegalStateException("You must check in before checking out"));

        if (attendance.getCheckInTime() == null) {
            throw new IllegalStateException("You must check in before checking out");
        }
        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("Already checked out today");
        }

        attendance.setCheckOutTime(getCurrentTime());
        Attendance saved = attendanceRepository.save(attendance);
        notificationService.notifyUser(user, "Check-Out Successful", "You checked out at " + saved.getCheckOutTime(), "/student/attendance/history");
        notificationService.notifyAdmins("Student Check-Out", user.getFullName() + " checked out at " + saved.getCheckOutTime(), "/admin/attendance");
        return saved;
    }

    private String savePhoto(MultipartFile file, String username) throws IOException {
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.')) : ".jpg";
        String filename = "attendance_" + username + "_" + System.currentTimeMillis() + ext;
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + filename;
    }

    private String saveLivePhoto(String photoData, String username) throws IOException {
        String base64 = photoData;
        int commaIndex = photoData.indexOf(',');
        if (commaIndex >= 0) {
            base64 = photoData.substring(commaIndex + 1);
        }
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        String filename = "attendance_" + username + "_" + System.currentTimeMillis() + ".jpg";
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        Files.write(dir.resolve(filename), imageBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return "/uploads/" + filename;
    }

    public List<Attendance> getHistory(User user) {
        return attendanceRepository.findByUserOrderByAttendanceDateDesc(user);
    }

    public List<Attendance> getTodayAll() {
        return attendanceRepository.findTodayWithUsers(getToday());
    }

    public List<Attendance> getAllByDateRange(LocalDate start, LocalDate end) {
        return attendanceRepository.findByAttendanceDateBetweenOrderByAttendanceDateDesc(start, end);
    }

    public long countPresentToday() {
        return attendanceRepository.countByAttendanceDateAndStatus(getToday(), AttendanceStatus.PRESENT);
    }

    public long countCheckedOutToday() {
        return attendanceRepository.countByAttendanceDateAndCheckOutTimeIsNotNull(getToday());
    }

    public List<Attendance> getRecentAttendance() {
        return attendanceRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public long countPresentForUser(User user) {
        return attendanceRepository.countByUserAndStatus(user, AttendanceStatus.PRESENT);
    }

    public long countAbsentForUser(User user) {
        return attendanceRepository.countByUserAndStatus(user, AttendanceStatus.ABSENT);
    }

    public Map<String, Object> getAnalyticsData(int days) {
        LocalDate end = getToday();
        LocalDate start = end.minusDays(days - 1L);
        List<Object[]> presentData = attendanceRepository.countPresentByDateRange(start, end, AttendanceStatus.PRESENT);

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (Object[] row : presentData) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("values", values);
        return result;
    }
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDateWithUsers(date);
    }

    public LocalDate getToday() {
        return LocalDate.now(Clock.system(ATTENDANCE_ZONE));
    }

    public LocalTime getCurrentTime() {
        return LocalTime.now(Clock.system(ATTENDANCE_ZONE));
    }
}
