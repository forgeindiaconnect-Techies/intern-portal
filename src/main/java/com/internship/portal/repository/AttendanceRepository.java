package com.internship.portal.repository;

import com.internship.portal.entity.Attendance;
import com.internship.portal.entity.AttendanceStatus;
import com.internship.portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("""
    SELECT a
    FROM Attendance a
    JOIN FETCH a.user
    WHERE a.attendanceDate = :date
    ORDER BY a.checkInTime DESC
""")
    List<Attendance> findTodayWithUsers(@Param("date") LocalDate date);

    @Query("""
    SELECT a
    FROM Attendance a
    JOIN FETCH a.user
    WHERE a.attendanceDate = :date
    ORDER BY a.checkInTime DESC
""")
    List<Attendance> findByAttendanceDateWithUsers(@Param("date") LocalDate date);

    Optional<Attendance> findByUserAndAttendanceDate(User user, LocalDate date);

    List<Attendance> findByUserOrderByAttendanceDateDesc(User user);

    List<Attendance> findByAttendanceDateOrderByCheckInTimeDesc(LocalDate date);

    List<Attendance> findByAttendanceDateBetweenOrderByAttendanceDateDesc(LocalDate start, LocalDate end);

    long countByAttendanceDateAndStatus(LocalDate date, AttendanceStatus status);

    long countByAttendanceDateAndCheckOutTimeIsNotNull(LocalDate date);

    long countByUserAndStatus(User user, AttendanceStatus status);

    @Query("SELECT a.attendanceDate, COUNT(a) FROM Attendance a WHERE a.status = :status " +
           "AND a.attendanceDate BETWEEN :start AND :end GROUP BY a.attendanceDate ORDER BY a.attendanceDate")
    List<Object[]> countPresentByDateRange(@Param("start") LocalDate start,
                                           @Param("end") LocalDate end,
                                           @Param("status") AttendanceStatus status);

    List<Attendance> findTop10ByOrderByCreatedAtDesc();

    List<Attendance> findByAttendanceDate(LocalDate date);

    void deleteByUser(User user);
}
