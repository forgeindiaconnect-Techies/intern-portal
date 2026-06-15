package com.internship.portal.service;

import com.internship.portal.entity.AttendanceWindow;
import com.internship.portal.repository.AttendanceWindowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AttendanceWindowService {

    private static final long WINDOW_ID = 1L;
    private static final LocalTime DEFAULT_START = LocalTime.of(11, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(12, 0);

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
        LocalTime now = LocalTime.now();
        return !now.isBefore(window.getStartTime()) && !now.isAfter(window.getEndTime());
    }

    public String getClosedMessage() {
        AttendanceWindow window = getWindow();
        return "Hey idiot, this is not the right time to mark your attendance. Today's class window is "
                + window.getStartTime() + " to " + window.getEndTime()
                + ". Come back tomorrow at the exact class time.";
    }
}
