package com.applebay.demo.dto;
import com.applebay.demo.entity.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
public record PaymentResponse(Long id, String receiptNumber, Long enrollmentId, BigDecimal amount, LocalDate paymentDate, PaymentMethod method) {}
