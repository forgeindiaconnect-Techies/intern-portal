package com.internship.portal.controller;

import com.internship.portal.entity.Attendance;
import com.internship.portal.entity.User;
import com.internship.portal.security.UserPrincipal;
import com.internship.portal.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AttendanceService attendanceService;
    private final FeedbackService feedbackService;
    private final NotificationService notificationService;
    private final CurrentUserHelper currentUserHelper;
    private final AttendanceWindowService attendanceWindowService;

    private void addCommonAttributes(Model model, User admin) {
        model.addAttribute("user", admin);
        model.addAttribute("unreadCount", notificationService.countUnread(admin));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        long totalStudents = userService.countAllStudents();
        long activeStudents = userService.countActiveStudents();
        long presentToday = attendanceService.countPresentToday();
        long checkedOutToday = attendanceService.countCheckedOutToday();
        long absentToday = activeStudents - presentToday;
        if (absentToday < 0) absentToday = 0;
        long feedbackToday = feedbackService.countSubmittedToday();

        addCommonAttributes(model, admin);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("activeStudents", activeStudents);
        model.addAttribute("presentToday", presentToday);
        model.addAttribute("absentToday", absentToday);
        model.addAttribute("checkedOutToday", checkedOutToday);
        model.addAttribute("feedbackToday", feedbackToday);
        model.addAttribute("recentFeedback", feedbackService.getRecentFeedback());
        model.addAttribute("notifications", notificationService.getRecentNotifications());
        model.addAttribute("analytics", attendanceService.getAnalyticsData(7));
        return "admin/dashboard";
    }

    @GetMapping("/students")
    public String students(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, admin);
        model.addAttribute("students", userService.getAllStudents());
        return "admin/students";
    }

    @GetMapping("/students/add")
    public String addStudentForm(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, admin);
        model.addAttribute("student", new User());
        return "admin/add-student";
    }

    @PostMapping("/students/add")
    public String addStudent(@ModelAttribute User student,
                             @RequestParam String password,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.createStudent(student, password);
            redirectAttributes.addFlashAttribute("success", "Student added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/students/add";
        }
        return "redirect:/admin/students";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudentForm(@PathVariable Long id, Model model,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        User student = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        addCommonAttributes(model, admin);
        model.addAttribute("student", student);
        return "admin/edit-student";
    }

    @PostMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, @ModelAttribute User student,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.updateStudent(id, student);
            redirectAttributes.addFlashAttribute("success", "Student updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/toggle/{id}")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User student = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            userService.setActive(id, !student.isActive());
            redirectAttributes.addFlashAttribute("success", "Student status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/reset-password/{id}")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam(defaultValue = "student123") String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password reset to: " + newPassword);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/upload-photo/{id}")
    public String uploadStudentPhoto(@PathVariable Long id,
                                     @RequestParam MultipartFile photo,
                                     RedirectAttributes redirectAttributes) {
        try {
            User student = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            userService.saveProfilePicture(student, photo);
            redirectAttributes.addFlashAttribute("success", "Profile picture uploaded!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @GetMapping("/attendance")
    public String attendance(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, admin);
        model.addAttribute("attendanceList", attendanceService.getTodayAll());
        model.addAttribute("presentToday", attendanceService.countPresentToday());
        model.addAttribute("selectedDate", attendanceService.getToday());
        return "admin/attendance";
    }
    @GetMapping("/attendance/date")
public String attendanceByDate(
        @RequestParam LocalDate date,
        Model model,
        @AuthenticationPrincipal UserPrincipal principal) {

    User admin = currentUserHelper.getCurrentUser(principal);

    addCommonAttributes(model, admin);

    model.addAttribute(
            "attendanceList",
            attendanceService.getAttendanceByDate(date));

    model.addAttribute(
            "presentToday",
            attendanceService.getAttendanceByDate(date).stream()
                    .filter(att -> att.getStatus() != null && att.getStatus().name().equals("PRESENT"))
                    .count());

    model.addAttribute(
            "selectedDate",
            date);

    return "admin/attendance";
}

    @GetMapping("/attendance/export")
    public void exportAttendance(@RequestParam LocalDate date, HttpServletResponse response) throws IOException {
        List<Attendance> attendanceList = attendanceService.getAttendanceByDate(date);
        String filename = "attendance-" + date + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("Attendance Report");
            title.createCell(1).setCellValue(date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

            Row header = sheet.createRow(2);
            String[] columns = {"Student Name", "Username", "Email", "College", "Department", "Check-In", "Check-Out", "Status"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 3;
            for (Attendance attendance : attendanceList) {
                Row row = sheet.createRow(rowIndex++);
                User student = attendance.getUser();
                row.createCell(0).setCellValue(student != null ? student.getFullName() : "Unknown Student");
                row.createCell(1).setCellValue(student != null ? student.getUsername() : "");
                row.createCell(2).setCellValue(student != null ? student.getEmail() : "");
                row.createCell(3).setCellValue(student != null && student.getCollege() != null ? student.getCollege() : "");
                row.createCell(4).setCellValue(student != null && student.getDepartment() != null ? student.getDepartment() : "");
                row.createCell(5).setCellValue(attendance.getCheckInTime() != null ? attendance.getCheckInTime().toString() : "");
                row.createCell(6).setCellValue(attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toString() : "");
                row.createCell(7).setCellValue(attendance.getStatus() != null ? attendance.getStatus().name() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/feedback")
    public String feedback(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, admin);
        model.addAttribute("feedbackList", feedbackService.getAllFeedback());
        model.addAttribute("feedbackToday", feedbackService.countSubmittedToday());
        return "admin/feedback";
    }

    @GetMapping("/analytics")
    public String analytics(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        Map<String, Object> analytics = attendanceService.getAnalyticsData(30);
        addCommonAttributes(model, admin);
        model.addAttribute("analytics", analytics);
        model.addAttribute("totalStudents", userService.countAllStudents());
        model.addAttribute("presentToday", attendanceService.countPresentToday());
        model.addAttribute("feedbackToday", feedbackService.countSubmittedToday());
        return "admin/analytics";
    }

    @GetMapping("/notifications")
    public String notifications(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, admin);
        model.addAttribute("notifications", notificationService.getNotificationsForUser(admin));
        return "admin/notifications";
    }

    @PostMapping("/notifications/read/{id}")
    public String markRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, currentUserHelper.getCurrentUser(principal));
        return "redirect:/admin/notifications";
    }

    @GetMapping("/notifications/open/{id}")
    public String openNotification(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        String targetUrl = notificationService.markAsReadAndGetTargetUrl(
                id,
                currentUserHelper.getCurrentUser(principal),
                "/admin/notifications");
        return "redirect:" + targetUrl;
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(currentUserHelper.getCurrentUser(principal));
        return "redirect:/admin/notifications";
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, admin);
        return "admin/profile";
    }

    @GetMapping("/settings")
    public String settings(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = currentUserHelper.getCurrentUser(principal);
        addCommonAttributes(model, admin);
        model.addAttribute("attendanceWindow", attendanceWindowService.getWindow());
        model.addAttribute("attendanceWindowOpen", attendanceWindowService.isOpenNow());
        return "admin/settings";
    }

    @PostMapping("/settings/attendance-window")
    public String updateAttendanceWindow(@RequestParam LocalTime startTime,
                                         @RequestParam LocalTime endTime,
                                         RedirectAttributes redirectAttributes) {
        try {
            attendanceWindowService.updateWindow(startTime, endTime);
            redirectAttributes.addFlashAttribute("success", "Attendance timing updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}
