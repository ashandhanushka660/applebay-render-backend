package com.applebay.demo.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
public record MarkAttendanceRequest(@NotNull Long courseId, @NotBlank String batch, @NotNull @PastOrPresent LocalDate date, @NotEmpty List<@Valid AttendanceRecordRequest> records) {}
