package com.applebay.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.applebay.demo.dto.BalanceResponse;
import com.applebay.demo.dto.PaymentRequest;
import com.applebay.demo.dto.PaymentResponse;
import com.applebay.demo.entity.Enrollment;
import com.applebay.demo.entity.Student;
import com.applebay.demo.exception.ResourceNotFoundException;
import com.applebay.demo.repository.EnrollmentRepository;
import com.applebay.demo.repository.StudentRepository;
import com.applebay.demo.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PaymentController {
    private final PaymentService service;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse record(@Valid @RequestBody PaymentRequest r) {
        return service.record(r.enrollmentId(), r.amount(), r.paymentDate(), r.method());
    }

    @GetMapping("/enrollment/{enrollmentId}")
    public List<PaymentResponse> getHistory(@PathVariable Long enrollmentId) {
        return service.getHistory(enrollmentId);
    }

    @GetMapping("/balance/{enrollmentId}")
    public BalanceResponse getBalance(@PathVariable Long enrollmentId) {
        return service.getBalance(enrollmentId);
    }

    @GetMapping("/lookup")
    public List<BalanceResponse> lookup(@RequestParam String query) {
        String q = query.trim();
        List<BalanceResponse> responses = new ArrayList<>();

        // If numeric ID directly passed
        if (q.matches("^\\d+$")) {
            Long directId = Long.parseLong(q);
            if (enrollmentRepository.existsById(directId)) {
                responses.add(service.getBalance(directId));
                return responses;
            }
        }

        // Search by student number or NIC
        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> s.getStudentNumber().equalsIgnoreCase(q) || s.getNic().equalsIgnoreCase(q))
                .toList();

        for (Student s : students) {
            List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                    .filter(e -> e.getStudent().getId().equals(s.getId()))
                    .toList();
            for (Enrollment e : enrollments) {
                responses.add(service.getBalance(e.getId()));
            }
        }

        if (responses.isEmpty()) {
            throw new ResourceNotFoundException("No active enrollments found for identifier: " + query);
        }

        return responses;
    }
}
