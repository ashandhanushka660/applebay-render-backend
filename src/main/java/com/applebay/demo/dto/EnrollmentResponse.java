package com.applebay.demo.dto;
import java.time.LocalDate;
public record EnrollmentResponse(Long id, Long studentId, String studentNumber, String studentName, Long courseId, String courseCode, String courseName, String batch, LocalDate enrolledOn) {}
