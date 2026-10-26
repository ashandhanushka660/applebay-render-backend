package com.applebay.demo.dto;
import com.applebay.demo.entity.CourseLevel;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record CourseRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 150) String name,
        @NotNull CourseLevel level,
        @NotNull @Positive Integer durationMonths,
        @NotNull @Positive BigDecimal totalFee
) {}
