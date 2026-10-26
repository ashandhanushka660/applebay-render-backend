package com.applebay.demo.dto;
import com.applebay.demo.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record FirstPaymentRequest(@NotNull @DecimalMin("0.01") BigDecimal amount, @PastOrPresent LocalDate paymentDate, @NotNull PaymentMethod method) {}
