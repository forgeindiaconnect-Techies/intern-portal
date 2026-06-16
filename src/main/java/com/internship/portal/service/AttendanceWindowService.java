package com.internship.portal.service;

import com.internship.portal.entity.AttendanceWindow;
import com.internship.portal.repository.AttendanceWindowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AttendanceWindowService {

    private static final long WINDOW_ID = 1L;
    private static final LocalTime DEFAULT_START = LocalTime.of(11, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(12, 0);
    private static final ZoneId ATTENDANCE_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("hh:mm a");

    private final AttendanceWindowRepository attendanceWindowRepository;

    @Transactional
    public AttendanceWindow getWindow() {
        return attendanceWindowRepository.findById(WINDOW_ID)
                .orElseGet(() -> attendanceWindowRepository.save(AttendanceWindow.builder()
                        .id(WINDOW_ID)
                        .startTime(DEFAULT_START)
                        .endTime(DEFAULT_END)
                        .build()));
    }

    @Transactional
    public AttendanceWindow updateWindow(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        AttendanceWindow window = getWindow();
        window.setStartTime(startTime);
        window.setEndTime(endTime);
        return attendanceWindowRepository.save(window);
    }

    public boolean isOpenNow() {
        AttendanceWindow window = getWindow();
        LocalTime now = getCurrentTime();
        return !now.isBefore(window.getStartTime()) && !now.isAfter(window.getEndTime());
    }

    public LocalTime getCurrentTime() {
        return LocalTime.now(Clock.system(ATTENDANCE_ZONE));
    }

    public String getClosedMessage() {
        AttendanceWindow window = getWindow();
        return "Attendance is open only during today's class window: "
                + window.getStartTime().format(DISPLAY_TIME) + " to "
                + window.getEndTime().format(DISPLAY_TIME)
                + ". Please come back during the scheduled class time.";
    }
}
