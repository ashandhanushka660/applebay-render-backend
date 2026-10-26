package com.applebay.demo.dto;
import com.applebay.demo.entity.CourseLevel;
import java.math.BigDecimal;
public record CourseResponse(Long id, String code, String name, CourseLevel level, Integer durationMonths, BigDecimal totalFee, boolean active) {}
