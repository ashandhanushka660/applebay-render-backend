package com.applebay.demo.dto;
import com.applebay.demo.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
public record AttendanceRecordRequest(@NotNull Long enrollmentId, @NotNull AttendanceStatus status) {}
