package com.applebay.demo.dto;
import com.applebay.demo.entity.AttendanceStatus;
public record ClassSheetRow(Long enrollmentId, String studentNumber, String studentName, AttendanceStatus status) {}
