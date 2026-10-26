package com.applebay.demo.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record RegistrationRequest(@Valid @NotNull StudentRequest student, @NotNull Long courseId, @NotBlank String batch, @Valid @NotNull FirstPaymentRequest firstPayment) {}
