package com.applebay.demo.dto;
import java.time.LocalDate;
public record StudentResponse(Long id, String studentNumber, String firstName, String lastName, String nic, String email, String phone, String address, LocalDate dateOfBirth) {}
