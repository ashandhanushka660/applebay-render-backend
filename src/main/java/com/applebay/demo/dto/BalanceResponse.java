package com.applebay.demo.dto;

import java.math.BigDecimal;

public record BalanceResponse(
    Long enrollmentId,
    String studentNumber,
    String studentName,
    String courseCode,
    String courseName,
    BigDecimal totalFee,
    BigDecimal totalPaid,
    BigDecimal outstandingBalance
) {}
