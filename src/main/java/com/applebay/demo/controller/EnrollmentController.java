package com.applebay.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.applebay.demo.dto.EnrollmentRequest;
import com.applebay.demo.dto.EnrollmentResponse;
import com.applebay.demo.entity.Enrollment;
import com.applebay.demo.repository.EnrollmentRepository;
import com.applebay.demo.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EnrollmentController {
    private final EnrollmentService service;
    private final EnrollmentRepository enrollmentRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(@Valid @RequestBody EnrollmentRequest req) {
        return service.enroll(req);
    }

    @GetMapping("/course/{courseId}")
    public List<EnrollmentResponse> getByCourse(@PathVariable Long courseId) {
        return enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse().getId().equals(courseId))
                .map(e -> new EnrollmentResponse(
                        e.getId(),
                        e.getStudent().getId(),
                        e.getStudent().getStudentNumber(),
                        e.getStudent().getFirstName() + " " + e.getStudent().getLastName(),
                        e.getCourse().getId(),
                        e.getCourse().getCode(),
                        e.getCourse().getName(),
                        e.getBatch(),
                        e.getEnrolledOn()
                ))
                .toList();
    }
}
