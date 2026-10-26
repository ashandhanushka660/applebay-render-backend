package com.applebay.demo.dto;
import java.math.BigDecimal;
public record EligibilityRow(Long enrollmentId, String studentNumber, String studentName, long sessions, long attended, BigDecimal percentage, boolean eligible) {}
