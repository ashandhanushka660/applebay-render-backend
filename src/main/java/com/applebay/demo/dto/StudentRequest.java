package com.applebay.demo.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record StudentRequest(
        @NotBlank @Size(max = 60) String firstName,
        @NotBlank @Size(max = 60) String lastName,
        @NotBlank @Pattern(regexp = "^([0-9]{9}[vVxX]|[0-9]{12})$", message = "Invalid NIC") String nic,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{9,15}$") String phone,
        @NotBlank @Size(max = 255) String address,
        @NotNull @Past LocalDate dateOfBirth
) {}
