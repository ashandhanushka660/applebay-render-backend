package com.applebay.demo.dto;
import java.time.LocalDate;
public record AttendanceMarkResponse(Long courseId, String batch, LocalDate date, int created, int updated, int unchanged) {}
